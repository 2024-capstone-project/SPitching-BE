package djj.spitching_be.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import djj.spitching_be.Domain.Presentation;
import djj.spitching_be.Domain.PresentationSlide;
import djj.spitching_be.Domain.User;
import djj.spitching_be.Dto.PresentationListResponseDto;
import djj.spitching_be.Dto.PresentationRequestDto;
import djj.spitching_be.Dto.PresentationResponseDto;
import djj.spitching_be.Dto.PresentationTitleUpdateRequestDto;
import djj.spitching_be.Repository.PresentationRepository;
import djj.spitching_be.Repository.PresentationSlideRepository;
import djj.spitching_be.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PresentationService {
    private final PresentationRepository presentationRepository;
    private final PresentationSlideRepository slideRepository;
    private final UserRepository userRepository;
    private final AmazonS3 amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public PresentationService(
            PresentationRepository presentationRepository,
            PresentationSlideRepository slideRepository,
            AmazonS3 amazonS3Client,
            UserRepository userRepository) {
        this.presentationRepository = presentationRepository;
        this.slideRepository = slideRepository;
        this.amazonS3Client = amazonS3Client;
        this.userRepository = userRepository;
    }

    // 발표 생성
    @Transactional
    public Presentation createPresentation(PresentationRequestDto requestDto, String userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Presentation presentation = new Presentation(requestDto, user);
        return presentationRepository.save(presentation);
    }

    // 특정 사용자의 발표 목록 조회
    public List<Presentation> getUserPresentations(String userId) {
        return presentationRepository.findByUserId(userId);
    }

    // 모든 발표 가져오기 - 삭제 예정
    public List<PresentationListResponseDto> findAllPresentation() {
        return presentationRepository.findAllByOrderByUpdatedAtDesc()
                .stream()
                .map(PresentationListResponseDto::new)
                .collect(Collectors.toList());
    }

    // 발표 하나 가져오기
    public PresentationResponseDto findOnePresentation(Long id){
        Presentation presentation = presentationRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("조회 실패")
        );
        return new PresentationResponseDto(presentation);
    }

    // 발표 수정 - 제목 수정
    @Transactional
    public String updatePresentation(Long id, PresentationTitleUpdateRequestDto requestDto) {
        Presentation presentation = presentationRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 아이디가 존재하지 않습니다.")
        );
        presentation.updateTitle(requestDto.getTitle());
        return "Updated";
    }

    // 발표 삭제
    @Transactional
    public String deletePresentation(Long id){
        presentationRepository.deleteById(id);
        return "Deleted";
    }

    // pdf 업로드 후 각 장을 png화
    public List<PresentationSlide> uploadAndConvertPdf(Long presentationId, MultipartFile file) throws IOException {
        // 해당 ID의 발표 연습을 찾음
        Presentation presentation = presentationRepository.findById(presentationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 발표 연습이 존재하지 않습니다."));

        // 임시 파일 생성
        File tempFile = File.createTempFile("pdf-", ".pdf");
        file.transferTo(tempFile);

        try (PDDocument document = PDDocument.load(tempFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            List<PresentationSlide> slides = new ArrayList<>();

            // PDF 파일을 S3에 업로드
            String pdfKey = "presentations/" + presentationId + "/original.pdf";
            amazonS3Client.putObject(new PutObjectRequest(bucketName, pdfKey, tempFile));

            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);

                // 이미지를 임시 파일로 저장
                File imageFile = File.createTempFile("slide-", ".png");
                ImageIO.write(bufferedImage, "PNG", imageFile);

                // 이미지를 S3에 업로드
                String imageKey = "presentations/" + presentationId + "/slides/slide_" + (page + 1) + ".png";
                amazonS3Client.putObject(new PutObjectRequest(bucketName, imageKey, imageFile));

                // S3 URL 생성
                String imageUrl = amazonS3Client.getUrl(bucketName, imageKey).toString();

                // 슬라이드 엔터티 생성
                PresentationSlide slide = new PresentationSlide();
                slide.setPresentation(presentation);
                slide.setSlideNumber(page + 1);
                slide.setImageUrl(imageUrl);  // S3 URL 저장
                slideRepository.save(slide);

                slides.add(slide);

                // 임시 이미지 파일 삭제
                imageFile.delete();
            }

            // 임시 PDF 파일 삭제
            tempFile.delete();

            return slides;
        }
    }
}

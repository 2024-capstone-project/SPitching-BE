package djj.spitching_be.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";  // login.html을 반환
    }

    @GetMapping("/loginSuccess")
    @ResponseBody  // 문자열을 직접 반환하기 위해 추가
    public String loginSuccess() {
        return "Login Successful!";
    }


//    @GetMapping("/loginSuccess")
//    public String loginSuccess() {
//        return "redirect:http://localhost:3000/dashboard";
//    }
}
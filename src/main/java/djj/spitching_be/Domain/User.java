package djj.spitching_be.Domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY) // 얘는 String과 함께 사용할 수 없음
    private String id;

//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
//    private List<Pet> pets = new ArrayList<>();

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private String picture;

    @Builder
    public User(String id, String name, String email, Role role, String picture) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.picture = picture;
    }

    public User update(String name, String picture) {
        this.name = name;
        this.picture = picture;
        return this;
    }
}

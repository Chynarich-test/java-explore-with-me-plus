package ru.yandex.practicum.user.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewUserRequest {
    @Email(message = "Некорректный Email")
    @NotBlank(message = "Email не должен быть пустым")
    private String email;

    private String name;
}
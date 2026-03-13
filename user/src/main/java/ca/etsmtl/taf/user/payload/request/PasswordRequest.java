package ca.etsmtl.taf.user.payload.request;


import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PasswordRequest {

  @NotBlank
  @Size(min = 6, max = 250)
  private String oldPassword;

  @NotBlank
  @Size(min = 6, max = 250)
  private String password;
}

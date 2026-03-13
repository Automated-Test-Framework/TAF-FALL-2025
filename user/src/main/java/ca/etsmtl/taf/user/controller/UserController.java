package ca.etsmtl.taf.user.controller;

import ca.etsmtl.taf.user.entity.User;
import ca.etsmtl.taf.user.payload.request.*;
import ca.etsmtl.taf.user.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

import ca.etsmtl.taf.user.payload.response.MessageResponse;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/home")
  public String greeting() {
    return "Hello from User Microservice!";
  }

  @Autowired
  private UserService userService;

  @GetMapping("/all")
  public String allAccess() {
    return "Bienvenue au TAF.";
  }

  @GetMapping("/user")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public String userAccess() {
    return "User Content.";
  }

  @GetMapping("/admin")
  @PreAuthorize("hasRole('ADMIN')")
  public String adminAccess() {
    return "Admin Board.";
  }

  @PostMapping("/create")
  public ResponseEntity<?> create(@RequestBody SignupRequest request) {
    if (this.userService.existsByUsername(request.getUsername())) {
      return ResponseEntity.badRequest().body("Username is already taken.");
    }

    if (this.userService.existsByEmail(request.getEmail())) {
      return ResponseEntity
              .badRequest()
              .body(new MessageResponse("Error: Email is already in use!"));
    }
    return ResponseEntity.ok(this.userService.save(request));
  }

  @PutMapping("/update")
  public ResponseEntity<?> update(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UpdateRequest request) {
    if (this.userService.existsByEmailAndIdNot(request.getEmail(), userDetails.getUsername()) ){
      return ResponseEntity.badRequest().body("Username is already taken.");
    }

    this.userService.update(userDetails.getUsername(), request);

    return ResponseEntity.ok(new MessageResponse("Inscription Réussie.!"));
  }

  @GetMapping("/profil")
  public ResponseEntity<User> get(@AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(this.userService.findByUsername(userDetails.getUsername()));
  }

  @PutMapping("/password")
  public ResponseEntity<?> get(@AuthenticationPrincipal UserDetails userDetails, @RequestBody PasswordRequest request) {
    int resp = this.userService.updatePassword(userDetails.getUsername(), request);
    if(resp == 0)
      return ResponseEntity.ok(new MessageResponse("Mot de passe Mis à jour!"));
    else if(resp == -1)
      return ResponseEntity.badRequest().body("Le mot de passe actuel n'est pas valide!");
    else
      return ResponseEntity.badRequest().body("Le nouveau mot de passe doit être différent de l'ancien.");
  }

}
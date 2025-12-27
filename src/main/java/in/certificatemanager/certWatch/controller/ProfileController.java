package in.certificatemanager.certWatch.controller;

import in.certificatemanager.certWatch.dto.AuthDTO;
import in.certificatemanager.certWatch.dto.ProfileDTO;
import in.certificatemanager.certWatch.service.ProfileService;
import in.certificatemanager.certWatch.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerProfile(@RequestBody ProfileDTO profileDTO){
        try{
            ProfileDTO registeredProfile = profileService.registerProfile(profileDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredProfile);
        }catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed. Email might be already used.");
        }
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateProfile(@RequestParam String token){
        boolean isActivated = profileService.activateProfile(token);
        if(isActivated){
            return ResponseEntity.ok("Profile activated successfully. You may close this tab.");
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Activation token not found or already used");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AuthDTO authDTO){
        try{
            if(!profileService.isAccountActive(authDTO.getEmail())){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "message", "Account is not active. Please activate your account first."
                ));
            }
            Map<String, Object> response = profileService.authenticateAndGenerateToken(authDTO);
            return ResponseEntity.ok(response);

        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/authCheck")
    public ResponseEntity<String> checkAuthStatus(@RequestParam String token){
        if(jwtUtil.validateJwtToken(token)){
            return ResponseEntity.ok("User is logged in.");
        }
        else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Auth is not in-place.");
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileDTO> getPublicProfile(){
        ProfileDTO profile =  profileService.getPublicProfile(null);
        return ResponseEntity.ok(profile);
    }

}

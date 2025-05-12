@Controller
@RequestMapping("/provocari")
public class ChallengeController {
    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @GetMapping
    public String listChallenges(Model model) {
        model.addAttribute("challenges", challengeService.getActiveChallenges());
        return "challenges/list";
    }

    @GetMapping("/{id}")
    public String viewChallenge(@PathVariable Long id, Model model) {
        model.addAttribute("challenge", challengeService.getChallengeById(id));
        return "challenges/view";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/participate")
    public String participateInChallenge(@PathVariable Long id, 
                                       @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((User) userDetails).getId();
        challengeService.participateInChallenge(id, userId);
        return "redirect:/provocari/" + id;
    }
}
package ti4.spring.api.games;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/public/games")
public class PublicGamesController {

    private final PublicGamesService publicGamesService;

    @GetMapping
    public List<GameSummary> get() {
        return publicGamesService.getGames();
    }
}

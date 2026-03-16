package ti4.spring.api.games;

import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import ti4.map.persistence.GameManager;
import ti4.map.persistence.ManagedGame;

@Service
class PublicGamesService {

    List<GameSummary> getGames() {
        return GameManager.getManagedGames().stream()
                .filter(ManagedGame::isActive)
                .map(ManagedGame::getGame)
                .filter(Objects::nonNull)
                .filter(game -> !game.isFowMode())
                .map(game -> new GameSummary(game.getName()))
                .toList();
    }
}

package ti4.spring.api.webdata;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ti4.map.persistence.GameManager;
import ti4.map.persistence.ManagedGame;
import ti4.website.AsyncTi4WebsiteHelper;

@Service
class PublicWebdataService {

    Map<String, Object> getWebdata(String gameId) {
        ManagedGame managedGame = GameManager.getManagedGame(gameId);
        if (managedGame == null || !managedGame.isActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        var game = managedGame.getGame();
        if (game == null || game.isFowMode()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return AsyncTi4WebsiteHelper.buildWebData(gameId, game);
    }
}

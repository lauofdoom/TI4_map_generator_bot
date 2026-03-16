package ti4.spring.api.webdata;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/public/game/{gameId}")
public class PublicWebdataController {

    private final PublicWebdataService publicWebdataService;

    @GetMapping("/webdata")
    public Map<String, Object> getWebdata(@PathVariable String gameId) {
        return publicWebdataService.getWebdata(gameId);
    }
}

package ti4.spring.context;

import java.io.IOException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * LOCAL CUSTOMIZATION: Serves game images from the bot classpath at /images/**, with automatic
 * format fallback from .webp → .png → .jpg so the web frontend can request modern WebP formats
 * while the bot retains original PNG/JPG assets.
 */
@Configuration
public class LocalWebConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource resource = super.getResource(resourcePath, location);
                        if (resource != null && resource.exists()) return resource;
                        if (resourcePath.endsWith(".webp")) {
                            String base = resourcePath.substring(0, resourcePath.length() - 5);
                            Resource pngResource = new ClassPathResource(base + ".png");
                            if (pngResource.exists()) return pngResource;
                            Resource jpgResource = new ClassPathResource(base + ".jpg");
                            if (jpgResource.exists()) return jpgResource;
                        }
                        return null;
                    }
                });
    }
}

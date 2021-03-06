package ca.qc.banq.gia.authentication.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

import static java.util.Objects.isNull;

/**
 * Configuration du traducteur
 *
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 13 sept. 2020
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TranslatorConfig {

    private final MessageSource messageSource;

    /**
     * Translate a key message
     */
    public String translate(String resolver, String language, Object... args) {
        try {
            return this.messageSource.getMessage(resolver, args, Locale.forLanguageTag(isNull(language) || language.isEmpty() ? "fr" : language));
        } catch (Exception e) {
            log.error("no entry found for key message: " + resolver);
            return resolver;
        }
    }

    /**
     * Translate a message key to default language
     */
    public String translate(String resolver, Object... args) {
        try {
            return this.messageSource.getMessage(resolver, args, Locale.FRENCH);
        } catch (Exception e) {
            log.error("no entry found for key message: " + resolver);
            return resolver;
        }
    }

}

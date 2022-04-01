/**
 *
 */
package ca.qc.banq.gia.authentication.rest;

import ca.qc.banq.gia.authentication.entities.App;
import ca.qc.banq.gia.authentication.models.AppPayload;
import ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Implementation des services web backoffice de gestion des applications BAnQ
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@RestController
@RequestMapping("/api/bo")
@RequiredArgsConstructor
@Api("Implementation des services web backoffice de gestion des applications")
public class GiaBackOfficeControllerImpl implements GiaBackOfficeController {

    private final GiaBackOfficeService business;

    /*
     * (non-javadoc)
     * @see ca.qc.banq.gia.authentication.rest.GiaBackOfficeController#saveApp(ca.qc.banq.gia.authentication.entities.App)
     */
    @Override
    @PostMapping("/sauvegarderApplication")
    @ApiOperation("Sauvegarder une application")
    public String saveApp(@RequestBody @Valid App app) {
        return business.saveApp(app);
    }

    /*
     * (non-javadoc)
     * @see ca.qc.banq.gia.authentication.rest.GiaBackOfficeController#deleteApp(java.lang.Long)
     */
    @Override
    @DeleteMapping("/supprimerApplication/{id}")
    @ApiOperation("Supprimer une application")
    public String deleteApp(@PathVariable("id") String id) {
        return business.deleteApp(id);
    }

    /*
     * (non-javadoc)
     * @see ca.qc.banq.gia.authentication.rest.GiaBackOfficeController#findAll()
     */
    @Override
    @GetMapping("/obtenirApplications")
    @ApiOperation("Afficher la liste des applications")
    public List<AppPayload> findAll() {
        return business.findAll();
    }

    /*
     * (non-javadoc)
     * @see ca.qc.banq.gia.authentication.rest.GiaBackOfficeController#findById(java.lang.Long)
     */
    @Override
    @GetMapping("/obtenirApplication/{id}")
    @ApiOperation("Retrouver une application a partir de son id")
    public AppPayload findById(@PathVariable("id") String id) {
        return business.findById(id);
    }
}

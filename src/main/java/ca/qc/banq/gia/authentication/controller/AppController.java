/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.qc.banq.gia.authentication.controller;

import ca.qc.banq.gia.authentication.entities.App;
import ca.qc.banq.gia.authentication.entities.AuthenticationType;
import ca.qc.banq.gia.authentication.models.AppPayload;
import ca.qc.banq.gia.authentication.repositories.GIARepository;
import ca.qc.banq.gia.authentication.services.GiaBackOfficeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

/**
 * Controlleur de gestion des applications dans la console d'administration du service d'authentification
 *
 * @author francis.djiomou
 * @since 2021-06-25
 */
@Controller
@RequiredArgsConstructor
class AppController {

    private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "apps/createOrUpdateAppForm";

    private final GIARepository giaRepository;

    private final GiaBackOfficeService giaBackOfficeService;

    @Value("${server.host}")
    private String serverHost;
    @Value("${server.servlet.context-path}")
    private String servletPath;

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    @GetMapping("/apps/new")
    public String initCreationForm(Map<String, Object> model) {
        App app = new App();
        app.setNouveau(true);
        model.put("app", app);
        model.put("types", Arrays.asList(AuthenticationType.values()));
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping("/apps/new")
    public String processCreationForm(@Valid App app, BindingResult result) {
        if (result.hasErrors()) {
            return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
        } else {
            this.giaBackOfficeService.createApp(app);
            return "redirect:/apps/" + app.getClientId();
        }
    }

    @GetMapping("/apps/find")
    public String initFindForm(Map<String, Object> model) {
        model.put("app", new App());
        return "apps/findApps";
    }

    @GetMapping("/apps")
    public String processFindForm(AppPayload app, BindingResult result, Map<String, Object> model) {

        // allow parameterless GET request for /apps to return all records
        if (isNull(app.getTitle())) app.setTitle("");

        // find apps by last name
        List<AppPayload> results = this.giaBackOfficeService.findByTitle(app.getTitle(), serverHost, servletPath);
        if (results.isEmpty()) {
            // no apps found
            //result.rejectValue("title", "notFound", "Aucune application trouvee");
            model.put("app", new App());
            return "apps/findApps";
        }
        if (results.size() == 1) {
            // 1 app found
            app = results.stream().findFirst().orElse(AppPayload.builder().build());//.iterator().next();
            return "redirect:/apps/" + app.getClientId();
        }
        // multiple apps found
        model.put("selections", results);
        return "apps/appsList";
    }

    @GetMapping("/apps/{appId}/edit")
    public String initUpdateAppForm(@PathVariable("appId") String appId, Model model) {
        App app = giaRepository.findById(appId).orElse(null);
        assert app != null;
        model.addAttribute(app);
        model.addAttribute("types", Arrays.asList(AuthenticationType.values()));
        //model.addAttribute("isnew", false);
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping("/apps/{appId}/edit")
    public String processUpdateAppForm(@Valid App app, BindingResult result, @PathVariable("appId") String appId) {
        if (result.hasErrors()) {
            return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
        } else {
            app.setClientId(appId);
            this.giaBackOfficeService.createApp(app);
            return "redirect:/apps/{appId}";
        }
    }

    /**
     * Custom handler for displaying an app.
     *
     * @param appId the ID of the app to display
     * @return a ModelMap with the model attributes for the view
     */
    @GetMapping("/apps/{appId}")
    public ModelAndView showApp(@PathVariable("appId") String appId) {
        ModelAndView mav = new ModelAndView("apps/appDetails");
        AppPayload appPayload = this.giaBackOfficeService.findByClientId(appId, serverHost, servletPath); //.apps.findById(appId).orElse(null);
        mav.addObject("appPayload", appPayload);
        return mav;
    }

    @RequestMapping("/apps/remove")
    public String processDeleteApp(HttpServletRequest request) {
        String appId = request.getParameter("appid");
        this.giaBackOfficeService.deleteApp(appId);
        return "redirect:/apps";
    }

}

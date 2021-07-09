// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package ca.qc.banq.gia.authentication.exceptions;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.boot.web.servlet.error.ErrorController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Configuration de la page d'erreur de l'application
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Controller
public class ErrorHandlerController implements ErrorController {

    private static final String PATH = "/error";

    /**
     * Servlet Mapping de la page d'erreur
     * @param req
     * @param response
     * @return
     */
    @RequestMapping(value = PATH)
    public ModelAndView returnErrorPage(HttpServletRequest req, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView("error2");
        mav.addObject("message", req.getAttribute("error"));
        return  mav;
    }

    /*
     * (non-javadoc)
     * @see org.springframework.boot.web.servlet.error.ErrorController#getErrorPath()
     */
    @Override
    public String getErrorPath() {
        return PATH;
    }

	@GetMapping("/oups")
	public ModelAndView triggerException() {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("message", "Expected: controller used to showcase what " + "happens when an exception is thrown");
        return  mav;
	}

}

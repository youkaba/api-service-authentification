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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import ca.qc.banq.gia.authentication.helpers.AuthHelperAAD;
import ca.qc.banq.gia.authentication.helpers.AuthHelperB2C;

/**
 * Pages de la console d'administration du service d'authentification
 * @author francis.djiomou
 * @since 2021-08-03
 */
@Controller
class WelcomeController {

	@Autowired
	AuthHelperB2C authHelperB2C;
	
	@Autowired
	AuthHelperAAD authHelperAAD;
	
	@Value("${server.host}")
	String serverHost;
	
	@Value("${server.servlet.context-path}")
	String servletPath;

	@Value("${spring.profiles.active}")
	String profile;

	/**
	 * Page d'accueil de la console d'administration du service d'authentification
	 * @return
	 */
	@GetMapping("/")
	public String welcome() {
		return "welcome";
	}

	/**
	 * Page d'affichage de l'environnement d'execution du service d'authentification
	 * @return
	 */
	@GetMapping("/env")
	public ModelAndView env() {
		ModelAndView mav = new ModelAndView("env");
		mav.addObject("b2c", authHelperB2C.getConfiguration());
		mav.addObject("aad", authHelperAAD.getConfiguration());
		mav.addObject("serverHost", serverHost);
		mav.addObject("servletPath", servletPath);
		mav.addObject("profile", profile);
		return mav;
	}

	/**
	 * Page documentation de la console d'administration du service d'authentification
	 * @return
	 */
	@GetMapping("/doc")
	public ModelAndView doc() {
		return new ModelAndView("doc");
	}
}

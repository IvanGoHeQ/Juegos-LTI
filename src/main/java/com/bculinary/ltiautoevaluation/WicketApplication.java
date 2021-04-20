package com.bculinary.ltiautoevaluation;

import org.apache.wicket.Application;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.core.util.file.WebApplicationPath;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.resource.CssUrlReplacer;
import org.apache.wicket.settings.ResourceSettings;
import org.wicketstuff.select2.ApplicationSettings;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

import com.bculinary.ltiautoevaluation.controller.HomePage;

import edu.ksu.lti.launch.model.LtiSession;
import edu.ksu.lti.launch.oauth.LtiPrincipal;


public class WicketApplication extends WebApplication {


	
	@Override
    public void init() {
		super.getComponentInstantiationListeners().add(new SpringComponentInjector(this));
		
		//Codigo para añadir el css al homepage
		ApplicationSettings.get().setIncludeJavascript(true).setIncludeCss(true);
		getResourceSettings().setCssCompressor(new CssUrlReplacer());
    }

	@Override
    public Class<? extends WebPage> getHomePage() {
        return HomePage.class;
    }
	
	
	public static WicketApplication get() {
        return (WicketApplication) Application.get();
    }
	
	

}
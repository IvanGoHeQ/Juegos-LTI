package com.bculinary.ltiautoevaluation.controller;

import com.bculinary.ltiautoevaluation.entity.*;

import edu.ksu.lti.launch.exception.NoLtiSessionException;
import edu.ksu.lti.launch.model.LtiLaunchData;
import edu.ksu.lti.launch.model.LtiSession;
import edu.ksu.lti.launch.oauth.LtiPrincipal;
import edu.ksu.lti.launch.service.SimpleLtiLoginService;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import com.bculinary.ltiautoevaluation.constants.EventConstants;
import com.bculinary.ltiautoevaluation.constants.LtiConstants;
import com.bculinary.ltiautoevaluation.constants.TemplateConstants;
import com.bculinary.ltiautoevaluation.service.EventTrackingService;
import com.bculinary.ltiautoevaluation.service.SecurityService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Controller
public class IndexController {



    @Autowired
    private EventTrackingService eventTrackingService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private SessionLocaleResolver localeResolver;

    @Value("${lti-autoevaluation.url:someurl}")
    private String canvasBaseUrl;

    @GetMapping("/")
    public ModelAndView index(@ModelAttribute LtiPrincipal ltiPrincipal, LtiSession ltiSession, Model model) {
    	log.info("aaa");
        LtiLaunchData lld = ltiSession.getLtiLaunchData();
        localeResolver.setDefaultLocale(new Locale(lld.getLaunchPresentationLocale()));
        return new ModelAndView(TemplateConstants.INDEX_TEMPLATE);
    }

    @GetMapping("/index")
    public ModelAndView index(
            @ModelAttribute LtiPrincipal ltiPrincipal,
            LtiSession ltiSession,
            Model model,
            @RequestParam(required=false) Boolean errors,
            HttpSession httpSession,
            @RequestParam("page") Optional<Integer> page,
            HttpServletRequest request
    ) {
    	log.info("bbb");

//    	  ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//          HttpServletRequest req = sra.getRequest();
//          HttpSession session = req.getSession();
//          LtiSession ltiSession = (LtiSession) session.getAttribute(SimpleLtiLoginService.class.getName());
//          if (ltiSession == null) {
//              throw new NoLtiSessionException();
//          }
//          return ltiSession;
    	
        try {
            LtiLaunchData lld = ltiSession.getLtiLaunchData();
            log.debug("Debugging ltiLaunch Object:\n"+ReflectionToStringBuilder.toString(lld));
            String canvasLoginId = ltiPrincipal.getUser();
            String canvasUserId = lld.getCustom().get(LtiConstants.CANVAS_USER_ID);
            String courseId = ltiSession.getCanvasCourseId();

            eventTrackingService.postEvent(EventConstants.LTI_LOGIN, canvasUserId, courseId);

            if(lld.getRolesList() == null || lld.getRolesList().isEmpty()) {
                throw new Exception(String.format("The user %s doesn't have any valid role.", canvasLoginId));
            }

            if(securityService.isStudent(lld.getRolesList())) {
                return handleStudentView(ltiPrincipal, ltiSession, model);
            }

            if (securityService.isFaculty(lld.getRolesList())) {
                return handleInstructorView(ltiPrincipal, ltiSession, model, page.orElse(1) - 1);
            }

        } catch(Exception ex) {
            log.error("Error displaying the LTI tool content.", ex);
        }

        return new ModelAndView(TemplateConstants.ERROR_TEMPLATE);
    }

  

    private ModelAndView handleInstructorView(@ModelAttribute LtiPrincipal ltiPrincipal, LtiSession ltiSession, Model model, int page) {
        
        String nombre= ltiPrincipal.getName(); 
        log.info("isInstructor");
        model.addAttribute("nombre",nombre);
        model.addAttribute("rol","Profesor");
        
        return new ModelAndView(TemplateConstants.INSTRUCTOR_TEMPLATE);
    }

    private ModelAndView handleStudentView(@ModelAttribute LtiPrincipal ltiPrincipal, LtiSession ltiSession, Model model) {
       
    	String nombre= ltiPrincipal.getName(); 
    	log.info("isStudent");
    	model.addAttribute("nombre",nombre);
        model.addAttribute("rol","Alumno");
      
        return new ModelAndView(TemplateConstants.INSTRUCTOR_TEMPLATE);
    }

}

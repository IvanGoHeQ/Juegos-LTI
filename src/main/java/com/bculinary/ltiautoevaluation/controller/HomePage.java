package com.bculinary.ltiautoevaluation.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.ContextRelativeResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.bculinary.Itiautoevaluation.modals.ModalPageAnyadir;
import com.bculinary.Itiautoevaluation.modals.ModalPageEditar;
import com.bculinary.ltiautoevaluation.entity.Juegos;
import com.bculinary.ltiautoevaluation.repository.JuegosRepository;
import com.bculinary.ltiautoevaluation.service.SecurityService;
import com.giffing.wicket.spring.boot.context.scan.WicketHomePage;

import edu.ksu.lti.launch.model.InstitutionRole;
import edu.ksu.lti.launch.model.LtiLaunchData;
import edu.ksu.lti.launch.model.LtiSession;
import edu.ksu.lti.launch.service.LtiLoginService;

/**
 * 
 * @author EDFPC-012
 * Clase principal de la aplicacion
 *
 */
@WicketHomePage
public class HomePage extends WebPage {
	
	
	@SpringBean
	 private SecurityService securityService;
	
    @SpringBean
    private LtiLoginService ltiLoginService;

	@SpringBean
	private JuegosRepository repository;

	// Metodo para recoger los juegos de la bd mediante el repositorio
	public List<Juegos> buscar() {

		Iterable<Juegos> it = repository.findAll();
		if (repository == null) {
			System.out.println("NULO");
		}
		repository.findAll();
		List<Juegos> juegos = new ArrayList<Juegos>();

		it.forEach(e -> juegos.add(e));
		return juegos;
	}
	
	//Metodo para añadir el css
	 @Override
	  public void renderHead(IHeaderResponse response) {
	    PackageResourceReference cssFile = new PackageResourceReference(this.getClass(), "Homepage.css");
	    CssHeaderItem cssItem = CssHeaderItem.forReference(cssFile);

	    response.render(cssItem);
	  }


	public HomePage() throws Exception {
		
		

		// Ventana para editar
		final ModalWindow modalEditar;
		
		add(modalEditar = new ModalWindow("modalEditar"));
		

		//Se cogen los datos que vienen del LTI
		LtiSession ltiSession = ltiLoginService.getLtiSession();
		LtiLaunchData lld = ltiSession.getLtiLaunchData();
		 List<InstitutionRole> rolesList = lld.getRolesList();
		
		

		// Creacion y rellenar la tabla con los juegos
		IModel lista2 = new LoadableDetachableModel() {
			protected Object load() {
				return buscar();
			}
		};


		//grupo de check's para comprobar los marcados
		final CheckGroup<Juegos> group = new CheckGroup<Juegos>("group", new ArrayList<Juegos>());

		
		//Se rellena la tabla con los datos de los juegos
		PageableListView<Juegos> lista = new PageableListView<Juegos>("listView", lista2, 6) {
			public void populateItem(ListItem<Juegos> item) {
				//variables y componentes
				Juegos juego = (Juegos) item.getModelObject();
				Label titulo;
				Label anyo;
				Label puntuacion;
				WebMarkupContainer tdCheck = new WebMarkupContainer("tdCheck");
				Check<Juegos> check;
				int y = 0;
				
				//se añaden al la lista los campos con valores
				item.add(anyo = new Label("anyoComponent", juego.getAnyo_publicacion()));
				item.add(puntuacion = new Label("puntuacionComponent", juego.getPuntuacion()));
				item.add(tdCheck);
				tdCheck.add(check = new Check<Juegos>("check", item.getModel()));
				
				String nota= juego.getPuntuacion().toString();
				nota= nota.replace(".", "");
				int nota1= Integer.parseInt(nota);
				
				//Dependiendo de la puntuacion la barra tiene un color u otro
				item.add(new Label("barra", Model.of("")){
				    @Override
				    protected void onComponentTag(ComponentTag tag) {
				        super.onComponentTag(tag);
				        String style = "width:"+nota1+"%";
				        String clase= tag.getAttribute("class");
				        
				        if(juego.getPuntuacion()<5 && juego.getPuntuacion()>2.5) {
				        	clase= clase+" bg-warning";
				        	tag.getAttributes().put("class", clase);
				        }else if(juego.getPuntuacion()<2.5) {
				        	clase= clase+" bg-danger";
				        	tag.getAttributes().put("class", clase);
				        }else if(juego.getPuntuacion()>=9) {
				        	clase= clase+" bg-success";
				        	tag.getAttributes().put("class", clase);
				        }
				        tag.getAttributes().put("style", style);
				    }
				});
				anyo.setOutputMarkupId(true);
				puntuacion.setOutputMarkupId(true);
				check.setOutputMarkupId(true);
				
				//Se le pone el link al titulo del juego que abre el modal de editar
				AjaxLink linkTitulo = new AjaxLink<Void>("link") {
					@Override
					public void onClick(AjaxRequestTarget target) {
						modalEditar.setPageCreator(new ModalWindow.PageCreator() {

							@Override
							public Page createPage() {
								return new ModalPageEditar(HomePage.this.getPageReference(), modalEditar, juego, repository);
							}
						});
						modalEditar.setDefaultModel(Model.of(juego));
						modalEditar.show(target);
					}
				};
				linkTitulo.add(titulo = new Label("tituloComponent", juego.getTitulo()));
				item.add(linkTitulo);
				titulo.setOutputMarkupId(true);

				//si el usuario es alumno se ocultan la fila de los checks y se deshabilitan los links del titulo
				if(securityService.isStudent(rolesList)) {
					tdCheck.setVisible(false);
					check.setVisible(false);
					linkTitulo.setEnabled(false);
		        }
				 
				
			}
		};
		

		lista.setOutputMarkupId(true);

		
		//Se añaden los componentes para formar la pagina
		WebMarkupContainer listContainer = new WebMarkupContainer("container");

		group.setOutputMarkupId(true);
		listContainer.setOutputMarkupId(true);
		
		Label eliminarTitulo;
		listContainer.add(lista);
		listContainer.add(new Label("tituloTitulo", "Título"));
		listContainer.add(new Label("anyoTitulo", "Año de publicación"));
		listContainer.add(new Label("puntuacionTitulo", "Puntuación"));
		listContainer.add(eliminarTitulo=new Label("eliminarTitulo", "Eliminar"));

		Form formulario = new Form("formularioTabla");
		formulario.add(group);

		

		

		// Creacion ventana añadir
		final ModalWindow modalAnyadir;
		add(modalAnyadir = new ModalWindow("modalAnyadir"));

		modalAnyadir.setPageCreator(new ModalWindow.PageCreator() {

			@Override
			public Page createPage() {
				return new ModalPageAnyadir(HomePage.this.getPageReference(), modalAnyadir);
			}
		});

		//Al cerrar la ventana añadir recargar la pagina
		modalAnyadir.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
			@Override
			public void onClose(AjaxRequestTarget target) {
				if (target != null) {
					target.add(listContainer);

				}
			}
		});

		//Al cerrar la ventana editar recargar la pagina
		modalEditar.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
			@Override
			public void onClose(AjaxRequestTarget target) {
				if (target != null) {

					target.add(listContainer);
				}
			}
		});
		
		
		//creacion componentes div
		WebMarkupContainer divAnyadir ;
		add(divAnyadir =new WebMarkupContainer("divAnyadir"));
		
		WebMarkupContainer divEliminar ;
		add(divEliminar =new WebMarkupContainer("divEliminar"));
	
		
		//Boton que muestra el modal de añadir
		AjaxLink link=new AjaxLink<Void>("showModalAnyadir") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				modalAnyadir.show(target);
			}
		};
		divAnyadir.add(link);
		
		//Botom que elimina los juegos seleccionados
		AjaxSubmitLink ajaxButton = new AjaxSubmitLink("buttonEliminar") {
			@Override
			protected void onSubmit(AjaxRequestTarget target) {

				if (group.getDefaultModelObject() != null) {
					ArrayList<Juegos> juegosSelected = (ArrayList<Juegos>) group.getDefaultModelObject();

					for (int i = 0; i < juegosSelected.size(); i++) {
						Long id = juegosSelected.get(i).getId_juego();
						repository.deleteById(id);
					}
				}

				setResponsePage(getPage());

			}
		};
		divEliminar.add(ajaxButton);
		
		if(rolesList == null || rolesList.isEmpty()) {
            throw new Exception(String.format("The user %s doesn't have any valid role."));
        }

		//Si el usuario es alumno se ocultan los botones de añadir y eliminar
        if(securityService.isStudent(rolesList)) {
        	divAnyadir.setVisible(false); 
        	divEliminar.setVisible(false); 
        	eliminarTitulo.setVisible(false);
        	
        }

        if (securityService.isFaculty(rolesList)) {
        
        }
		
		//Se añaden mas componentes
        formulario.setOutputMarkupId(true);
		formulario.add(divEliminar);
		group.add(new AjaxPagingNavigator("navigator", lista));
		group.add(listContainer);
		add(formulario);
        
	}

}

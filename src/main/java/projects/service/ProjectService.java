package projects.service;

import java.util.List;
import java.util.NoSuchElementException;

import projects.dao.ProjectDao;
import projects.entity.Project;
import projects.exception.DbException;

//The service layer in this small application is implemented by a single file, 
//ProjectService.java. Mostly this file acts as a pass-through between the 
//main application file that runs the menu (ProjectsApp.java) and the 
//DAO file in the data layer (ProjectDao.java).
public class ProjectService {

	private ProjectDao projectDao = new ProjectDao();
	
	
	/**Calls the DAO class to insert a record into the project table.
	*
	* @param project the {@link Project} object.
	* @return The Project object with a generated key value.
	*/
	
	//adds a project to the db
	public Project addProject(Project project) {
		
		return projectDao.insertProject(project); 
		
	}

	//Returns all projects from db
	public List<Project> fetchAllProjects() {
		
		return projectDao.fetchAllProjects();
	}
	
	//Returns the project based on project id
	public Project fetchProjectById(Integer projectId) {
		return projectDao.fetchByProjectId(projectId).orElseThrow(()-> new NoSuchElementException(
				"Project with id=" + projectId + " does not exist.\n"));
	}

	public void modifyProjectDetails(Project project) {
		//Throw exception if update fails
		if(!projectDao.modifyProjectDetails(project)) {
			throw new DbException("Project with ID="+project.getProjectId()+ " does not exist.");
		}
		
	}

	public void deleteProject(Integer projectId) {
		//Throw exception if delete fails
		if(!projectDao.deleteProject(projectId)) {
			throw new DbException("Project with ID="+projectId+ " does not exist.");
		}
		
	}




}

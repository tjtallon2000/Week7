package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;


//Performsa CRUD operations using JDBC
public class ProjectDao extends DaoBase {

	private static final String CATEGORY_TABLE = "category";
	private static final String MATERIAL_TABLE = "material";
	private static final String PROJECT_TABLE = "project";
	private static final String PROJECT_CATEGORY_TABLE = "project_category";
	private static final String STEP_TABLE = "step";

	/**
	 * Insert into project table
	 * 
	 * @param project object to insert
	 * @return The Project object with the primary key.
	 * @throws DbException if an error occurs when inserting into row.
	 */

	//Gets all projects from db
	public List<Project> fetchAllProjects() {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";
		
		//Get db Connection
		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			
			//Try sql statement
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				
				//Execute the sql statement
				try (ResultSet rs = stmt.executeQuery()) {
					
					//Create list that holds all projects in db
					List<Project> projects = new LinkedList<>();
					while (rs.next()) {
						projects.add(extract(rs, Project.class));
					}
					return projects;
				} 
				//If it fails do not execute partial transaction 
				catch (Exception e) {
					rollbackTransaction(conn);
					throw new DbException(e);
				}

			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	public Project insertProject(Project project) {
		// formatter:off
		String sql = "INSERT INTO " + PROJECT_TABLE + " "
				+ "(project_name, estimated_hours, actual_hours, difficulty, notes) " + "VALUES " + "(?, ?, ?, ?, ?)";
		// formatter:on'
		
		//Get db Connection
		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			
			//Try sql statement
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);

				//Try to commit transaction
				stmt.executeUpdate();
				Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
				commitTransaction(conn);

				project.setProjectId(projectId);
				return project;

			} 
			
			//If it fails do not execute partial transaction
			catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	public Optional<Project> fetchByProjectId(Integer projectId) {
		String sql = "SELECT * FROM "+PROJECT_TABLE+" WHERE project_id = ?";
		
		//Get db connection
		try(Connection conn=DbConnection.getConnection()){
			startTransaction(conn);
			try {
				Project project =null;
				
				//Exectue sql statement
				try(PreparedStatement stmt=conn.prepareStatement(sql)){
					setParameter(stmt,1,projectId,Integer.class);			
					try(ResultSet rs =stmt.executeQuery()){
						if(rs.next()) {
							project=extract(rs,Project.class);
						}
					}
				}
				
				if(Objects.nonNull(conn)) {
					project.getMaterials().addAll(fetchMaterialsForProject(conn,projectId));
					project.getSteps().addAll(fetchStepsForProject(conn,projectId));
					project.getCategories().addAll(fetchCategoriesForProject(conn,projectId));
				}
				
				//Return project info
				commitTransaction(conn);
				return Optional.ofNullable(project);
			}
			catch(Exception e) {
				//If it fails do not execute partial transaction
				rollbackTransaction(conn);
				throw new DbException("Project with id="+projectId+ " does not exist.");
			}
			
		}
		catch(SQLException e) {
			throw new DbException(e);
		}
		
		}

	private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) throws SQLException {
		// @formatter:off
		String sql=""
				+"SELECT c.* FROM "+CATEGORY_TABLE+ " c "
				+"JOIN "+PROJECT_CATEGORY_TABLE+" pc USING (category_id)"
				+"WHERE project_id = ? ";				
		// @formatter:on
		
		//Exectue sql statement
		try(PreparedStatement stmt=conn.prepareStatement(sql)){
			setParameter(stmt,1,projectId,Integer.class);			
			try(ResultSet rs =stmt.executeQuery()){
				List<Category> categories = new LinkedList<>();
				while(rs.next()) {
					categories.add(extract(rs,Category.class));
				}
				return categories;
			}
		}
	}

	private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
		// @formatter:off
		String sql=""
				+"SELECT * FROM "+STEP_TABLE+" WHERE project_id = ? ";				
		// @formatter:on
		
		//Exectue sql statement
		try(PreparedStatement stmt=conn.prepareStatement(sql)){
			setParameter(stmt,1,projectId,Integer.class);			
			try(ResultSet rs =stmt.executeQuery()){
				List<Step> steps = new LinkedList<>();
				while(rs.next()) {
					steps.add(extract(rs,Step.class));
				}
				return steps;
			}
		}
	}

	private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId) throws SQLException {
		// @formatter:off
		String sql=""
				+"SELECT * FROM "+MATERIAL_TABLE+" WHERE project_id = ? ";				
		// @formatter:on
		
		//Exectue sql statement
		try(PreparedStatement stmt=conn.prepareStatement(sql)){
			setParameter(stmt,1,projectId,Integer.class);			
			try(ResultSet rs =stmt.executeQuery()){
				List<Material> materials = new LinkedList<>();
				while(rs.next()) {
					materials.add(extract(rs,Material.class));
				}
				return materials;
			}
		}
		

	}

	public boolean modifyProjectDetails(Project project) {
		// @forammter:off
		String sql=""
				+"UPDATE "+PROJECT_TABLE+" SET "
				+"project_name = ?, "
				+"estimated_hours = ?, "
				+"actual_hours = ?, "
				+"difficulty = ?, "
				+"notes = ? "
				+"WHERE project_id = ? ";
		// @forammter:on
		
		//Get db connection
		try(Connection conn=DbConnection.getConnection()){
			startTransaction(conn);
			
			//Exectue sql statement
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);
				setParameter(stmt, 6, project.getProjectId(), Integer.class);
				
				//Returns true if 1 row is modiied
				boolean modified=stmt.executeUpdate() == 1;
				commitTransaction(conn);
				return modified;
				
			}catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
			
		} catch (SQLException e) {
	
			throw new DbException(e);
		}
		
		
		
		
		
		
		
	}



	public boolean deleteProject(Integer projectId) {
		String sql = "DELETE FROM "+PROJECT_TABLE+" WHERE project_id = ?";
		
		//Get db connection
		try(Connection conn=DbConnection.getConnection()){
			startTransaction(conn);
			
			//Exectue sql statement
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				setParameter(stmt, 1, projectId, Integer.class);
				
				//Returns true if 1 row is deleted
				boolean deleted=stmt.executeUpdate()==1;
				commitTransaction(conn);
				return deleted;
				
			}catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
			
		} catch (SQLException e) {
	
			throw new DbException(e);
		}
		
		
	}



}
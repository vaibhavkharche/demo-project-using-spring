package com.vk.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlQuery;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.vk.bos.EmployeeBO;

@Named("empDAO")
public class EmployeeDAOImpl implements EmployeeDAO {
	
	private static final String GET_ALL_EMPS = "SELECT * FROM VMK_EMPLOYEES"; 
	private static final String GET_EMPS_BY_DESG = "SELECT * FROM VMK_EMPLOYEES WHERE JOB=?";
	private static final String UPDATE_SAL_BY_PERCENT = "UPDATE VMK_EMPLOYEES SET SALARY=? WHERE EMPNO=?";
	private static final String GET_EMP_SAL_BY_NO = "SELECT SALARY FROM VMK_EMPLOYEES WHERE EMPNO=?";
	private static final String GET_EMPS_BY_SAL_RANGE = "SELECT EMPNO, ENAME, JOB, SALARY FROM VMK_EMPLOYEES WHERE SALARY BETWEEN :min AND :max";
	
	AllEmployeeSelector aes = null;
	EmployeeSelectorUsingAddress esa = null;
	EmployeeSalaryUpdator esu = null;
	JdbcTemplate jt = null;
	NamedParameterJdbcTemplate npjt = null;
	SimpleJdbcInsert sji = null;

	@Inject
	public EmployeeDAOImpl(DataSource ds) {
		aes = new AllEmployeeSelector(ds, GET_ALL_EMPS);
		esa = new EmployeeSelectorUsingAddress(ds, GET_EMPS_BY_DESG);
		esu = new EmployeeSalaryUpdator(ds, UPDATE_SAL_BY_PERCENT);
		jt = new JdbcTemplate(ds);
		npjt = new NamedParameterJdbcTemplate(ds);
		sji = new SimpleJdbcInsert(ds);
	}
	
	//	returns all Employees
	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, noRollbackFor = { SQLException.class, DuplicateKeyException.class})
	public List<EmployeeBO> getAllEmployees() {
		return aes.execute();		//.getAllEmps();
	}
	
	// returns Employees by Designation
	@Override
	public List<EmployeeBO> getEmpsByDesg(String desg) {
		return esa.execute(desg);			//getEmpsByDesg(desg);
	}
	
	// updates Employee salary by percentage
	@Override
	public double updateSalByPercent(int eno, int perc) {
		return esu.updateSalary(eno, perc);
	}
	 
	//	returns giver employee no. Employee's salary 
	@Override
	public double getEmpSalaryByNo(int eno) {
		return jt.queryForObject(GET_EMP_SAL_BY_NO, Double.class, eno);
	}
	
	//	returns Employees falls within the given range of salary
	@Override
	public List<EmployeeBO> getEmpsBySalRange(double min, double max) {
		List<EmployeeBO> listBO = new ArrayList<>();
		
		Map<String, Double> map = new HashMap<String, Double>();
		map.put("min", min);
		map.put("max", max);
		
		npjt.query(GET_EMPS_BY_SAL_RANGE, map, new EmpRowCallbackHandler(listBO));
		
		return listBO;
	}
	//callback handler for GET_EMPS_BY_SAL_RANGE
	class EmpRowCallbackHandler implements RowCallbackHandler{
		private List<EmployeeBO> listBO = null;
		public EmpRowCallbackHandler(List<EmployeeBO> listBO) {
			this.listBO = listBO;
		}
		
		@Override
		public void processRow(ResultSet rs) throws SQLException {
			listBO.add(new EmployeeBO(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4) ));
		}
		
	}
	
	//	insert Employee in DB
	@Override
	public int insertEmployee(EmployeeBO bo) {
		sji.setTableName("VMK_EMPLOYEES");
		BeanPropertySqlParameterSource source = new BeanPropertySqlParameterSource(bo);
		return sji.execute(source);
	}
	
//	-------------------------Inner classes----------------------	
	
	private static final class AllEmployeeSelector extends MappingSqlQuery<EmployeeBO> {
		
		public AllEmployeeSelector(DataSource ds, String qry) {
			super(ds,qry);
			super.compile();
		}
		
		@Override
		protected EmployeeBO mapRow(ResultSet rs, int rowNo) throws SQLException {
			return new EmployeeBO
				(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4));
		}
		
		/*public List<EmployeeBO> getAllEmps() {
			List<EmployeeBO> listBO = super.execute();
			return listBO;
		}*/
		
	}//inner class
	
	private static final class EmployeeSelectorUsingAddress extends MappingSqlQuery<EmployeeBO> {
		
		public EmployeeSelectorUsingAddress(DataSource ds, String qry) {
			super(ds, qry);
			super.declareParameter(new SqlParameter(Types.VARCHAR));
			super.compile();
		}
		
		@Override
		protected EmployeeBO mapRow(ResultSet rs, int index) throws SQLException {
			return new EmployeeBO
				(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4));
		}
		
		public List<EmployeeBO> getEmpsByDesg(String desg){
			List<EmployeeBO> listBO = super.execute(desg);
			return listBO;
		}
	}//inner class
	
	private final class EmployeeSalaryUpdator extends SqlUpdate {
		
		public EmployeeSalaryUpdator(DataSource ds, String qry) {
			super(ds,qry);
			super.declareParameter(new SqlParameter(Types.INTEGER));
			super.declareParameter(new SqlParameter(Types.INTEGER));
			super.compile();
		}
		
		public double updateSalary(int eno, int per) {
			int count = 0;
			double sal = getEmpSalaryByNo(eno);
//			double sal = getJdbcTemplate().queryForObject(GET_EMP_SAL_BY_NO, Double.class, eno);
			
			sal = sal + (sal*per/100);
			count = super.update(sal,eno);
			
			if(count != 0)
				return sal;
			else
				return 0.0;
		}
	}//INNER CLASS

}

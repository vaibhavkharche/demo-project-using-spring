package com.vk.daos;

import java.util.List;

import com.vk.bos.EmployeeBO;

public interface EmployeeDAO {
	public List<EmployeeBO> getAllEmployees();
	public List<EmployeeBO> getEmpsByDesg(String desg);
	public double updateSalByPercent(int eno, int perc);
	public double getEmpSalaryByNo(int eno);
	public List<EmployeeBO> getEmpsBySalRange(double min, double max);
	public int insertEmployee(EmployeeBO bo);
}

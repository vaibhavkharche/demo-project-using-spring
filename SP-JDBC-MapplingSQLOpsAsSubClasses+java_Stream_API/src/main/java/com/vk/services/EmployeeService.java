package com.vk.services;

import java.util.List;

import com.vk.dtos.EmployeeDTO;

public interface EmployeeService {
	public List<EmployeeDTO> getAllEmployees();
	public List<EmployeeDTO> getEmpsByDesg(String desg);
	public String updateSalByPercent(int eno, int perc);
	public double getEmpSalaryByNo(int eno);
	public List<EmployeeDTO> getEmpsBySalRange(double min, double max);
	public String registerEmployee(int empno, String ename, String job, double salary);
}

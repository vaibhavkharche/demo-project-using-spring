package com.vk.services;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.inject.Named;

import com.vk.bos.EmployeeBO;
import com.vk.daos.EmployeeDAO;
import com.vk.dtos.EmployeeDTO;

@Named("empService")
public class EmployeeServiceImpl implements EmployeeService {

	@Resource
	EmployeeDAO dao;
	public List<EmployeeDTO> getAllEmployees() {
		List<EmployeeDTO> listDTO = new ArrayList<EmployeeDTO>();
		List<EmployeeBO> listBO = null;
		EmployeeDTO edto = null;
		long startn, endn;
		
		//get employees
		listBO = dao.getAllEmployees();
		
		/*
		 startn = System.currentTimeMillis();
		for(EmployeeBO bo : listBO){
			edto = new EmployeeDTO();
			BeanUtils.copyProperties(bo, edto);
			listDTO.add(edto);
		}
		endn = System.currentTimeMillis();
		//This has better performance
		System.out.println("Time taken by advance for loop:: " + (endn-startn) + " ms"); //2559934ns
		 */
		//--------OR---------
		
		startn = System.currentTimeMillis();	//start time
		
		/*listBO.forEach(bo ->{
			EmployeeDTO dto = new EmployeeDTO();
			BeanUtils.copyProperties(bo, dto);
			listDTO.add(dto);
		});*/
		
		listDTO = listBO.stream()
				.map(e -> { EmployeeDTO d = new EmployeeDTO();
							BeanUtils.copyProperties(e, d);
							return d; })
				.collect(Collectors.toList());

		endn = System.currentTimeMillis();	//end time
		
		System.out.println("Time taken by lamda:: " + (endn-startn) + " ms"); //42492303ns
		
		return listDTO;
	}
	
	@Override
	public List<EmployeeDTO> getEmpsByDesg(String desg) {
		List<EmployeeDTO> listDTO = new ArrayList<EmployeeDTO>();
		List<EmployeeBO> listBO = dao.getEmpsByDesg(desg);
		EmployeeDTO dto = null;
		
		for(EmployeeBO bo : listBO) {
			dto = new EmployeeDTO();
			BeanUtils.copyProperties(bo, dto);
			listDTO.add(dto);
		}
		
		return listDTO;
	}
	
	@Override
	public double getEmpSalaryByNo(int eno) {
		return dao.getEmpSalaryByNo(eno);
	}
	
	@Override
	public String updateSalByPercent(int eno, int perc) {
		double sal = dao.updateSalByPercent(eno, perc);
		if(sal == 0.0)
			return "Salary Updation Failed..";
		else
			return eno + " Employee's Salary Updated successfully by " + perc + "%, new Salary is: " + sal;
	}
	
	@Override
	public List<EmployeeDTO> getEmpsBySalRange(double min, double max) {
		List<EmployeeDTO> listDTO = null;
		
		List<EmployeeBO> listBO = dao.getEmpsBySalRange(min, max);
		
		listDTO = listBO.stream()
						.map(bo -> {
							EmployeeDTO d = new EmployeeDTO();
							BeanUtils.copyProperties(bo, d);
							return d;
						})
						.collect(Collectors.toList());
		
		return listDTO;
	}
	
	@Override
	public String registerEmployee(int empno, String ename, String job, double salary) {
		int count = 0;
		count = dao.insertEmployee(new EmployeeBO(empno, ename, job, salary));
		
		if(count > 0)
			return empno + " Employee Registered Successfully.";
		else
			return empno + " Employee Registration Failed..!";
	}

}

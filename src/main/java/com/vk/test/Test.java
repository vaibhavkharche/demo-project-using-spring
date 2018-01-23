package com.vk.test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.vk.dtos.EmployeeDTO;
import com.vk.services.EmployeeService;

public class Test {

	public static void main(String[] args) {
		ApplicationContext ctx = null;
		EmployeeService service = null;
		List<EmployeeDTO> emps = null;
		
		ctx = new FileSystemXmlApplicationContext("src/main/java/com/vk/cfgs/applicationContext.xml");
		service = ctx.getBean("empService", EmployeeService.class);
		
//		get all employees
		emps = service.getAllEmployees();
		System.out.println("-------------------------------------------------------");
		
		System.out.println("All Employees Details:");
		emps.forEach(System.out::println);
		System.out.println("-------------------------------------------------------");
		
		System.out.println("All Employees Details sorted by Employee Number:");
		emps.stream().sorted((e1, e2) -> e1.getEmpno() - e2.getEmpno()).forEach(System.out::println);
		System.out.println("-------------------------------------------------------");
		
		double totSal = emps.stream()
							.collect(Collectors.summingDouble(EmployeeDTO::getSalary));
		System.out.println("Total Salary paid to All Employees: " + totSal);
		System.out.println("-------------------------------------------------------");
		
		Map<String, Double> jobWiseSal =
				emps.stream()
				.collect(Collectors.groupingBy(EmployeeDTO::getJob,
						Collectors.summingDouble(EmployeeDTO::getSalary)));
		System.out.println("Job wise total Salary:\n" + jobWiseSal);
		System.out.println("-------------------------------------------------------");
		
		System.out.println("Employees \"Name-->Salary\" Partitioned By Salary: 30000");
		Map<Boolean, List<String>> hiLowSal = 
				emps.stream()
				.collect(Collectors.partitioningBy(e -> e.getSalary() >= 30000,
						Collectors.mapping((e -> e.getEname()+ "-->" + e.getSalary()), Collectors.toList() )) );
		hiLowSal.entrySet().forEach(System.out::println);
		System.out.println("-------------------------------------------------------");
		
		System.out.println("Employees partitioned by there name's 1st char less than k or not");
		Map<Boolean, List<String> > empsByChars =
				emps.stream()
					.collect(Collectors.partitioningBy(e -> Character.toLowerCase(e.getEname().charAt(0)) - 'k' >= 0,
							Collectors.mapping(EmployeeDTO::getEname, Collectors.toList()) ));
		empsByChars.entrySet().forEach(System.out::println);
		System.out.println("-------------------------------------------------------");
		
		String joinedEnames = 
				emps.stream()
				.map(EmployeeDTO::getEname)
				.collect(Collectors.joining(", "));
		System.out.println("All Employee Names: " + joinedEnames);
		System.out.println("-------------------------------------------------------");
		
		System.out.println("Group By Desg:");
		Map<String, List<EmployeeDTO>> byJob =
				emps.stream()
				.collect(Collectors.groupingBy(EmployeeDTO::getJob));
		byJob.entrySet().forEach(System.out::println);
		System.out.println("-------------------------------------------------------");
		
//		-----get employees by designation
		System.out.println("All Employees By Designation: \"developer\"");
		service.getEmpsByDesg("developer").forEach(System.out::println);
		System.out.println("-------------------------------------------------------");
		
		System.out.println("1005 Employee's salary: " + service.getEmpSalaryByNo(1));
		System.out.println("-------------------------------------------------------");
		
		System.out.println(service.updateSalByPercent(1, 10));
		System.out.println("-------------------------------------------------------");
		
		System.out.println("Employees By Salary Range 40000-60000");
		service.getEmpsBySalRange(40000, 60000)
				.forEach(System.out::println);
		System.out.println("-------------------------------------------------------");
		
//		System.out.println(service.registerEmployee(103, "haresh", "developer", 80000));
		
		((AbstractApplicationContext) ctx).close();

	}

}

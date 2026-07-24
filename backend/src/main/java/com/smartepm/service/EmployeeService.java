package com.smartepm.service;

import com.smartepm.dto.request.EmployeeRequest;
import com.smartepm.dto.response.EmployeeResponse;
import com.smartepm.dto.response.PageResponse;

import java.util.List;

public interface EmployeeService {
    EmployeeResponse createEmployee(EmployeeRequest request);
    EmployeeResponse updateEmployee(Long id, EmployeeRequest request);
    void deleteEmployee(Long id);
    EmployeeResponse restoreEmployee(Long id);
    List<EmployeeResponse> getDeletedEmployees();
    EmployeeResponse getEmployeeById(Long id);
    PageResponse<EmployeeResponse> searchEmployees(String keyword, String department, int page, int size, String sortBy, String direction);
}

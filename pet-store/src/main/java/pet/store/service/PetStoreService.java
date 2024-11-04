package pet.store.service;

import java.util.LinkedList;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pet.store.controller.model.PetStoreData;
import pet.store.controller.model.PetStoreData.PetStoreCustomer;
import pet.store.controller.model.PetStoreData.PetStoreEmployee;
import pet.store.dao.CustomerDao;
import pet.store.dao.EmployeeDao;
import pet.store.dao.PetStoreDao;
import pet.store.entity.Customer;
import pet.store.entity.Employee;
import pet.store.entity.PetStore;

@Service
public class PetStoreService {

  @Autowired
  private PetStoreDao petStoreDao;

  @Autowired
  private EmployeeDao employeeDao;

  @Autowired
  private CustomerDao customerDao;

  public PetStoreData savePetStore(PetStoreData petStoreData) {
    Long petStoreId = petStoreData.getPetStoreId();
    PetStore petStore = findOrCreatePetStore(petStoreId);
    copyPetStoreFields(petStore, petStoreData);

    return new PetStoreData(petStoreDao.save(petStore));
  }

  private void copyPetStoreFields(PetStore petStore, PetStoreData petStoreData) {

    petStore.setPetStoreId(petStoreData.getPetStoreId());
    petStore.setPetStoreName(petStoreData.getPetStoreName());
    petStore.setPetStoreAddress(petStoreData.getPetStoreAddress());
    petStore.setPetStoreCity(petStoreData.getPetStoreCity());
    petStore.setPetStoreState(petStoreData.getPetStoreState());
    petStore.setPetStoreZip(petStoreData.getPetStoreZip());
    petStore.setPetStorePhone(petStoreData.getPetStorePhone());

  }

  private PetStore findOrCreatePetStore(Long petStoreId) {
    PetStore petStore;

    if (Objects.isNull(petStoreId)) {
      petStore = new PetStore();
    } else {
      petStore = findPetStoreById(petStoreId);
    }

    return petStore;
  }

  private PetStore findPetStoreById(Long petStoreId) {

    return petStoreDao.findById(petStoreId).orElseThrow(
        () -> new NoSuchElementException("Pet store with ID= " + petStoreId + " was not found."));
  }

  @Transactional(readOnly = false)
  public PetStoreEmployee saveEmployee(Long petStoreId, PetStoreEmployee petStoreEmployee) {

    PetStore petStore = findPetStoreById(petStoreId);

    Employee employee = findOrCreateEmployee(petStoreId, petStoreEmployee.getEmployeeId());
    copyEmployeeFields(employee, petStoreEmployee);

    employee.setPetStore(petStore); // not for customers
    petStore.getEmployees().add(employee);
    // 73 customer.getPetStore().add(petStore)
    Employee dbEmployee = employeeDao.save(employee);
    return new PetStoreEmployee(dbEmployee);
  }


  private Employee findOrCreateEmployee(Long petStoreId, Long employeeId) {
    Employee employee;

    if (Objects.isNull(employeeId)) {
      employee = new Employee();
    } else {
      employee = findEmployeeById(petStoreId, employeeId);
    }

    return employee;
  }



  private Employee findEmployeeById(Long petStoreId, Long employeeId) {

    Employee employee = employeeDao.findById(employeeId).orElseThrow(
        () -> new NoSuchElementException("Employee with ID=" + employeeId + " Does not exist."));

    if (employee.getPetStore().getPetStoreId() != petStoreId) {
      throw new IllegalArgumentException(
          "Employee with ID=" + employeeId + " is not part of store with ID=" + petStoreId);
    }
    return employee;
  }

  private void copyEmployeeFields(Employee employee, PetStoreEmployee petStoreEmployee) {

    employee.setEmployeeId(petStoreEmployee.getEmployeeId());
    employee.setEmployeeFirstName(petStoreEmployee.getEmployeeFirstName());
    employee.setEmployeeLastName(petStoreEmployee.getEmployeeLastName());
    employee.setEmployeePhone(petStoreEmployee.getEmployeePhone());
    employee.setEmployeeJobTitle(petStoreEmployee.getEmployeeJobTitle());

  }


  @Transactional(readOnly = false)
  public PetStoreCustomer saveCustomer(Long petStoreId, PetStoreCustomer petStoreCustomer) {

    PetStore petStore = findPetStoreById(petStoreId);

    Customer customer = findOrCreateCustomer(petStoreId, petStoreCustomer.getCustomerId());

    copyCustomerFields(customer, petStoreCustomer);

    customer.getPetStores().add(petStore);

    Customer dbCustomer = customerDao.save(customer); // interface

    return new PetStoreCustomer(dbCustomer);
  }



  private Customer findOrCreateCustomer(Long petStoreId, Long customerId) {
    Customer customer;

    if (Objects.isNull(customerId)) {
      customer = new Customer();
    } else {
      customer = findCustomerById(petStoreId, customerId);
    }
    return customer;
  }

  private Customer findCustomerById(Long petStoreId, Long customerId) {

    Customer customer = customerDao.findById(customerId).orElseThrow(
        () -> new NoSuchElementException("Customer with ID=" + customerId + " does not exist."));

    boolean isCustomerInStore = customer.getPetStores().stream()
        .anyMatch(petStore -> petStore.getPetStoreId().equals(petStoreId));

    if (!isCustomerInStore) {
        throw new IllegalArgumentException("Customer with ID=" + customerId 
            + " is not associated with store with ID=" + petStoreId);
    }
    return customer;
  }


  private void copyCustomerFields(Customer customer, PetStoreCustomer petStoreCustomer) {

    customer.setCustomerId(petStoreCustomer.getCustomerId());
    customer.setCustomerFirstName(petStoreCustomer.getCustomerFirstName());
    customer.setCustomerLastName(petStoreCustomer.getCustomerLastName());
    customer.setCustomerEmail(petStoreCustomer.getCustomerEmail());

  }


@Transactional(readOnly = true)
public List<PetStoreData> retrieveAllPetStores() {
  
  List<PetStore> petStores = petStoreDao.findAll();
  List<PetStoreData> response = new LinkedList<>();
  
  for(PetStore petStore : petStores) {
    PetStoreData psd = new PetStoreData(petStore);
      psd.getCustomers().clear();
      psd.getEmployees().clear();
      
    response.add(psd);
  }
  
  
  return response;
}

@Transactional(readOnly = true)
public PetStoreData retrievePetStoreById(Long petStoreId) {
  
  PetStore petStore = findPetStoreById(petStoreId);
  
  return new PetStoreData(petStore);
}

public void deletePetStoreById(Long petStoreId) {
  PetStore petStore = findPetStoreById(petStoreId);
  petStoreDao.delete(petStore);

  
}
}

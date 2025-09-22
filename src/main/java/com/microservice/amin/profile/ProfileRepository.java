package com.microservice.amin.profile;



import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends MongoRepository<Profile, String> {

   
  
    Profile findByUserId(String userId);
    
}



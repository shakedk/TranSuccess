package transSuccess.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import transSuccess.repository.FilesRepository;


@Service
public class TransSuccessService {

	@Autowired
	FilesRepository filesRepository;
	
	public JsonNode getTelAvivAreas(){
		return filesRepository.getTelAvivAreas();
	}
	
}

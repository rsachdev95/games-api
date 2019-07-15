package com.rsachdev.Games.API.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsachdev.Games.API.exception.ServiceException;
import com.rsachdev.Games.API.model.Developer;
import com.rsachdev.Games.API.model.Developers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class AmazonS3Service {
    private static final String BUCKET = "ch-senior-dev-test";
    private static final String FILE = "developers.json";

    @Autowired
    private AmazonS3 amazonS3Client;

    private List<Developer> authorisedDevelopers;

    public List<Developer> getAuthorisedDevelopers() throws ServiceException {
        if(authorisedDevelopers == null) {
            S3ObjectInputStream inputStream;

            try {
                inputStream = getDevelopers();
                authorisedDevelopers = unmarshallJson(inputStream);
            } catch (IOException | AmazonS3Exception e) {
                throw new ServiceException("Error when retrieving list of authorised developers", e);
            }
        }

        return authorisedDevelopers;
    }

    private S3ObjectInputStream getDevelopers() throws AmazonS3Exception {
        S3Object object;

        try {
            object = amazonS3Client.getObject(BUCKET, FILE);
        } catch(AmazonS3Exception ase) {
            throw new AmazonS3Exception(FILE + " or " + BUCKET + " doesn't exist", ase);
        }

        return object.getObjectContent();
    }

    private List<Developer> unmarshallJson(S3ObjectInputStream objectStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Developers developers;

        try {
            developers = mapper.readValue(objectStream, Developers.class);
        } catch (IOException ioe) {
            throw new  IOException("Error occurred unmarshalling json", ioe);
        }

        return developers.getDevelopers();
    }
}

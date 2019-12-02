package org.activiti.cloud.services.rest.assemblers;

import java.util.ArrayList;
import java.util.List;

import org.activiti.cloud.api.process.model.impl.CandidateUser;

public class ToCandidateUserConverter {

    public List<CandidateUser> from (List<String> users){
        List<CandidateUser> list = new ArrayList();
        users.forEach(u -> list.add(new CandidateUser(u)));
        return list;
    }
}

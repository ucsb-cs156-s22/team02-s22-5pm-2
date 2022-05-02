package edu.ucsb.cs156.example.controllers;
//HelpRequestController.java in controllers
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.time.LocalDateTime;

@Api(description = "HelpRequest")
@RequestMapping("/api/HelpRequest")
@RestController
@Slf4j
public class HelpRequestController extends ApiController {

    @Autowired
    HelpRequestRepository helpRequestRepository;
//get mapping /all
    @ApiOperation(value = "List all help requests")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<HelpRequest> allHelpRequest() {
        Iterable<HelpRequest> helpRequest = helpRequestRepository.findAll();
        return helpRequest;
    }
//get a single request by id
    @ApiOperation(value = "Get a single help request")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public HelpRequest getById(
            @ApiParam("id") @RequestParam Long id) {
                HelpRequest helpRequest = helpRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));

        return helpRequest;
    }
//post mapping 
    @ApiOperation(value = "Create a new help request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public HelpRequest postHelpRequest(
            @ApiParam("requesterEmail") @RequestParam String requesterEmail,
            @ApiParam("teamId") @RequestParam String teamId,
            @ApiParam("tableOrBreakoutRoom") @RequestParam String tableOrBreakoutRoom,

            @ApiParam("date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("requestTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime requestTime,
            @ApiParam("explanation") @RequestParam String explanation,
            @ApiParam("solved") @RequestParam Boolean solved
    )
            throws JsonProcessingException {


        log.info("requestTime={}", requestTime);

        HelpRequest helpRequest = new HelpRequest();

        helpRequest.setRequesterEmail(requesterEmail);
        helpRequest.setTeamId(teamId);
        helpRequest.setTableOrBreakoutRoom(tableOrBreakoutRoom);
        helpRequest.setRequestTime(requestTime);
        helpRequest.setExplanation(explanation);
        helpRequest.setSolved(solved);

        HelpRequest savedRequest = helpRequestRepository.save(helpRequest);

        return savedRequest;
    }

//delete
    @ApiOperation(value = "Delete a request")
    @PreAuthorize("hasRole('ROLE_ADMIN')") //only admin can delete
    @DeleteMapping("")
    public Object deleteHelpRequest(
            @ApiParam("id") @RequestParam Long id) {
        HelpRequest helpRequest = helpRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));

                helpRequestRepository.delete(helpRequest);
        return genericMessage("HelpRequest with id %s deleted".formatted(id));
    }
//update single 
    @ApiOperation(value = "Update a single request")
    @PreAuthorize("hasRole('ROLE_ADMIN')") //only admin can update
    @PutMapping("")
    public HelpRequest updateHelpRequest(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid HelpRequest incoming) {

        HelpRequest helpRequest = helpRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));


        helpRequest.setRequesterEmail(incoming.getRequesterEmail());
        helpRequest.setTeamId(incoming.getTeamId());
        helpRequest.setTableOrBreakoutRoom(incoming.getTableOrBreakoutRoom());
        helpRequest.setRequestTime(incoming.getRequestTime());
        helpRequest.setExplanation(incoming.getExplanation());
        helpRequest.setSolved(incoming.getSolved());


        helpRequestRepository.save(helpRequest);
        return helpRequest;
    }
}
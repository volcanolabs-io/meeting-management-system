package io.volcanolabs.medium.mms.controllers;

import io.volcanolabs.medium.mms.domain.Meeting;
import io.volcanolabs.medium.mms.domain.MeetingForInsert;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.volcanolabs.medium.mms.domain.Meeting.StatusEnum.CREATED;

@RestController
@RequestMapping( "/v1/meetings" )
public class MeetingsController {

	private final List<Meeting> meetings = new ArrayList<>();

	public MeetingsController() {
		var meeting = new Meeting()
				.id( UUID.fromString( "1b303df2-8517-4add-8e19-cf5e67cb40ad" ) )
				.name( "Meeting #1" )
				.location( "123 4th ST, BigCity, FL 12345" )
				.status( CREATED );

		meetings.add( meeting );
	}


	@GetMapping
	public List<Meeting> getMeetings() {
		return meetings;
	}

	@PostMapping
	Meeting addMeeting(@RequestBody @Valid MeetingForInsert insertMeeting) {
		var meeting = new Meeting()
				.id( UUID.randomUUID() )
				.name( insertMeeting.getName() )
				.location( insertMeeting.getLocation() )
				.dateTime( insertMeeting.getDateTime() )
				.duration( insertMeeting.getDuration() )
				.status( CREATED );

		meetings.add( meeting );

		return meeting;
	}

	@PutMapping
	Meeting updateMeeting(@RequestBody @Valid Meeting meeting) {
		var storedMeetingOpt = meetings.stream()
				.filter( m -> m.getId().equals( meeting.getId() ) )
				.findFirst();

		if ( storedMeetingOpt.isPresent() ) {
			var storedMeeting = storedMeetingOpt.get();

			storedMeeting
					.name( meeting.getName() )
					.location( meeting.getLocation() )
					.dateTime( meeting.getDateTime() )
					.duration( meeting.getDuration() )
					.status( meeting.getStatus() );

			return storedMeeting;
		} else {
			throw new ResponseStatusException( HttpStatus.NOT_FOUND );
		}
	}

	@DeleteMapping( "/{uuid}" )
	@ResponseStatus( HttpStatus.NO_CONTENT )
	public void deleteMeeting(@PathVariable UUID uuid) {
		var elementDeleted = meetings.removeIf( meeting -> meeting.getId().equals( uuid ) );

		if ( !elementDeleted ) {
			throw new ResponseStatusException( HttpStatus.NOT_FOUND );
		}
	}

	/**
	 * Don't do this at home. I'm doing this to make the testing easier for
	 * simplicity of explaining the concept.
	 */
	public void emptyMeetings() {
		meetings.clear();
	}

	/**
	 * Don't do this at home. I'm doing this to make the testing easier for
	 * simplicity of explaining the concept.
	 */
	public void loadMeetings(Meeting... meetingsToInsert) {
		meetings.addAll( List.of( meetingsToInsert ) );
	}
}

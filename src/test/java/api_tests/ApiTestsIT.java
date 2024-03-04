package api_tests;

import io.volcanolabs.medium.mms.MMSApplication;
import io.volcanolabs.medium.mms.controllers.MeetingsController;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test.mms.ApiClient;
import test.mms.ApiException;
import test.mms.api.MeetingsApi;
import test.mms.domain.MeetingForInsert;

import java.math.BigDecimal;
import java.util.UUID;

import static io.volcanolabs.medium.mms.domain.Meeting.StatusEnum.CREATED;
import static java.math.BigDecimal.valueOf;
import static java.time.OffsetDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@SpringBootTest(
		classes = MMSApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
public class ApiTestsIT {

	public static final String TEST_UUID = "1b303df2-8517-4add-8e19-cf5e67cb40ad";

	public static final String SOME_MEETING = "some meeting";
	public static final String LOCATION = "123 4th St";
	static MeetingsApi meetingsApi;

	@Autowired
	MeetingsController meetingsController;

	@BeforeAll
	static void init() {
		ApiClient apiClient = new ApiClient();

		meetingsApi = new MeetingsApi( apiClient );
	}

	@BeforeEach
	void testSetup() {
		var testMeeting = new io.volcanolabs.medium.mms.domain.Meeting()
				.id( UUID.fromString( TEST_UUID ) )
				.name( SOME_MEETING )
				.status( CREATED )
				.location( LOCATION )
				.dateTime( now() )
				.duration( BigDecimal.valueOf( 1.5 ) );

		meetingsController.emptyMeetings();
		meetingsController.loadMeetings( testMeeting );
	}

	@Test
	void getMeetings() throws ApiException {
		var meetings = meetingsApi.getMeetings();

		assertNotNull( meetings );

		var theMeeting = meetings.getFirst();
		assertEquals( TEST_UUID, theMeeting.getId().toString() );
	}

	@Test
	void addMeeting() throws ApiException {
		var testMeeting = new MeetingForInsert()
				.name( "some other meeting" )
				.location( "123 4th St" )
				.dateTime( now() )
				.duration( valueOf( 1.0 ) );

		var meeting = meetingsApi.addMeeting( testMeeting );
		assertNotNull( meeting.getId() );
		assertEquals( "some other meeting", meeting.getName() );

		var allMeetings = meetingsApi.getMeetings();
		assertEquals( 2, allMeetings.size() );
	}

	@Test
	void updateMeeting() throws ApiException {
		var currentMeetings = meetingsApi.getMeetings();

		var firstMeeting = currentMeetings.getFirst();
		firstMeeting.location( "Somewhere else" );

		var testMeeting = meetingsApi.getMeetings().getFirst();
		assertEquals( "Somewhere else", testMeeting.getLocation() );
	}

	@Test
	void deleteMeeting() throws ApiException {
		var initialMeetings = meetingsApi.getMeetings();
		assertEquals( 1, initialMeetings.size() );

		meetingsApi.deleteMeeting( TEST_UUID );

		var currentMeetings = meetingsApi.getMeetings();
		assertEquals( 0, currentMeetings.size() );
	}
}

package io.volcanolabs.medium.mms;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.volcanolabs.medium.mms.controllers.MeetingsController;
import io.volcanolabs.medium.mms.domain.Meeting;
import io.volcanolabs.medium.mms.domain.MeetingForInsert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import test.mms.ApiClient;
import test.mms.api.MeetingsApi;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static io.volcanolabs.medium.mms.domain.Meeting.StatusEnum.CREATED;
import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest
@AutoConfigureMockMvc
class MMSApplicationTests {

	public static final String TEST_UUID = "1b303df2-8517-4add-8e19-cf5e67cb40ad";
	public static final String ZEROS_UUID = "00000000-0000-0000-0000-000000000000";

	public static final String SOME_MEETING = "some meeting";

	public static final String LOCATION = "123 4th St";

	@Autowired
	MockMvc mockMvc;

	WebTestClient webTestClient;

	static MeetingsApi meetingsApi;

	@Autowired
	MeetingsController meetingsController;

	@Autowired
	ObjectMapper objectMapper;

	Meeting testMeeting;

	@BeforeAll
	static void init() {
		test.mms.ApiClient apiClient = new ApiClient();

		meetingsApi = new MeetingsApi( apiClient );
	}

	@BeforeEach
	void setup() {
		webTestClient = MockMvcWebTestClient.bindTo( mockMvc ).build();

		testMeeting = new io.volcanolabs.medium.mms.domain.Meeting()
				.id( UUID.fromString( TEST_UUID ) )
				.name( SOME_MEETING )
				.status( CREATED )
				.location( LOCATION )
				.dateTime( OffsetDateTime.parse( "2007-12-03T10:15:30+01:00" ) )
				.duration( BigDecimal.valueOf( 1.5 ) );

		meetingsController.emptyMeetings();
		meetingsController.loadMeetings( testMeeting );
	}

	@Test
	void whenGetMeetingsCalled_thenReturnAllMeetings() {
		var meetings = webTestClient
				.get()
				.uri( "/v1/meetings" )
				.accept( APPLICATION_JSON )

				.exchange()
				.expectStatus().isOk()

				.expectBodyList( Meeting.class )
				.returnResult()
				.getResponseBody();

		assert meetings != null;
		var firstMeeting = meetings.getFirst();
		assertEquals( TEST_UUID, firstMeeting.getId().toString() );
		assertEquals( "some meeting", firstMeeting.getName() );
		assertEquals( "123 4th St", firstMeeting.getLocation() );
	}

	@Test
	void whenPost_thenReturnMeetingAfterStoring() {
		var when = OffsetDateTime.parse( "2017-12-03T10:15:30+01:00" );

		var meetingForInsert = new MeetingForInsert()
				.name( "blah blah" )
				.location( "Over the rainbow" )
				.dateTime( when )
				.duration( valueOf( 1.5 ) )
				.status( MeetingForInsert.StatusEnum.CREATED );

		var meeting = webTestClient
				.post()
				.uri( "/v1/meetings" )
				.accept( APPLICATION_JSON )
				.bodyValue( meetingForInsert )

				.exchange()
				.expectStatus().isOk()

				.expectBody( Meeting.class )
				.returnResult()
				.getResponseBody();

		assert meeting != null;
		assertNotNull( meeting.getId() );
		assertEquals( "blah blah", meeting.getName() );
		assertEquals( "Over the rainbow", meeting.getLocation() );
		assertEquals( 2017, meeting.getDateTime().getYear() );
		assertEquals( BigDecimal.valueOf( 1.5 ), meeting.getDuration() );
		assertEquals( Meeting.StatusEnum.CREATED, meeting.getStatus() );
	}

	@Test
	void whenPut_thenUpdateMeeting() {
		var updateMeeting = new Meeting()
				.id( testMeeting.getId() )
				.name( "Some other meeting")
				.location( testMeeting.getLocation() )
				.duration( testMeeting.getDuration() )
				.status( testMeeting.getStatus() );

		var updatedMeeting = webTestClient
				.put()
				.uri( "/v1/meetings" )
				.accept( APPLICATION_JSON )
				.bodyValue( updateMeeting )

				.exchange()
				.expectStatus().isOk()

				.expectBody( Meeting.class )
				.returnResult()
				.getResponseBody();

		assert updatedMeeting != null;
		assertEquals( UUID.fromString( TEST_UUID ), updatedMeeting.getId() );
		assertEquals( "Some other meeting", updatedMeeting.getName() );
	}

	@Test
	void whenDelete_thenRemoveMeeting() {
		webTestClient
				.delete()
				.uri( "/v1/meetings/" + TEST_UUID )
				.accept( APPLICATION_JSON )

				.exchange()
				.expectStatus().isNoContent();
	}

	@Test
	void whenDeleteDoesntExist_thenNotFound() {
		webTestClient
				.delete()
				.uri( "/v1/meetings/" + ZEROS_UUID )
				.accept( APPLICATION_JSON )

				.exchange()
				.expectStatus().isNotFound();
	}
}

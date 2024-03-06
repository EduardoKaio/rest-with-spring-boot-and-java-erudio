package com.example.demo.integrationtests.controller.withxml;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.configs.TestConfigs;
import com.example.demo.integrationTests.vo.AccountCredentialsVO;
import com.example.demo.integrationTests.vo.BookVO;
import com.example.demo.integrationTests.vo.TokenVO;
import com.example.demo.integrationTests.vo.pagedmodels.PagedModelBook;
import com.example.demo.integrationtests.testcontainers.AbstractIntegrationTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(OrderAnnotation.class)
public class BookControllerXmlTest extends AbstractIntegrationTest{

	private static RequestSpecification specification;
	private static  XmlMapper objectMapper;
	
	private static BookVO book;
	
	@BeforeAll
	public static void setup() {
		objectMapper = new XmlMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		book = new BookVO();
	}
	
	@Test
	@Order(0)
	public void authorization() throws JsonMappingException, JsonProcessingException {
		
		AccountCredentialsVO user = new AccountCredentialsVO("leandro", "admin123");
		
		
		
		var accessToken = given()
				.basePath("/auth/signin")
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_XML)
				.body(user)
					.when()
				.post()
					.then()
						.statusCode(200)
							.extract()
							.body()
								.as(TokenVO.class).getAccessToken();
		
		specification = new RequestSpecBuilder()
				.addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + accessToken)
				.setBasePath("/api/book/v1")
				.setPort(TestConfigs.SERVER_PORT)
					.addFilter(new RequestLoggingFilter(LogDetail.ALL))
					.addFilter(new ResponseLoggingFilter(LogDetail.ALL))
				.build();

	}
	
	@Test
	@Order(1)
	public void testCreate() throws JsonMappingException, JsonProcessingException {
		mockBook();
		
		var content = given().spec(specification)
				.contentType(TestConfigs.CONTENT_TYPE_XML)
				.accept(TestConfigs.CONTENT_TYPE_XML)
					.body(book)
					.when()
					.post()
				.then()
					.statusCode(200)
						.extract()
							.body()
								.asString();
		
		book = objectMapper.readValue(content, BookVO.class);
		
		assertNotNull(book);
		assertNotNull(book.getId());
		assertNotNull(book.getAuthor());
		assertNotNull(book.getLaunchDate());
		assertNotNull(book.getPrice());
		assertNotNull(book.getTitle());
		
		assertTrue(book.getId() > 0);
		
		assertEquals("Robert C. Martin", book.getAuthor());
//		assertEquals(new Date(), book.getLaunchDate());
		assertEquals(77.00, book.getPrice());
		assertEquals("Clean Code", book.getTitle());

		
	}
	@Test
	@Order(2)
	public void testUpdate() throws JsonMappingException, JsonProcessingException {
		book.setTitle("Clean Code X");;
		
		var content = given().spec(specification)
				.contentType(TestConfigs.CONTENT_TYPE_XML)
				.accept(TestConfigs.CONTENT_TYPE_XML)
					.body(book)
					.when()
					.put()
				.then()
					.statusCode(200)
						.extract()
						.body()
							.asString();
		
		 BookVO bookUpdated = objectMapper.readValue(content, BookVO.class);
		
		assertNotNull(bookUpdated);
		assertNotNull(bookUpdated.getAuthor());
		assertNotNull(bookUpdated.getLaunchDate());
		assertNotNull(bookUpdated.getPrice());
		assertNotNull(bookUpdated.getTitle());
		
		assertEquals(book.getId(), bookUpdated.getId());
		
		assertEquals("Robert C. Martin", bookUpdated.getAuthor());
//		assertEquals(new Date(), bookUpdated.getLaunchDate());
		assertEquals(77.00, bookUpdated.getPrice());
		assertEquals("Clean Code X", bookUpdated.getTitle());

		
	}
	
	
	@Test
	@Order(3)
	public void testFindById() throws JsonMappingException, JsonProcessingException {
		mockBook();
		
		var content = given().spec(specification)
				.contentType(TestConfigs.CONTENT_TYPE_XML)
				.accept(TestConfigs.CONTENT_TYPE_XML)
					.pathParam("id", book.getId())
					.when()
					.get("{id}")
				.then()
					.statusCode(200)
						.extract()
						.body()
							.asString();
		
		BookVO foundBook = objectMapper.readValue(content, BookVO.class);
		
		assertNotNull(foundBook);
		assertNotNull(foundBook.getId());
		assertNotNull(foundBook.getAuthor());
		assertNotNull(foundBook.getLaunchDate());
		assertNotNull(foundBook.getPrice());
		assertNotNull(foundBook.getTitle());
		
		assertEquals(book.getId(), foundBook.getId());
		
		assertEquals("Robert C. Martin", foundBook.getAuthor());
//		assertEquals(new Date(), foundBook.getLaunchDate());
		assertEquals(77.00, foundBook.getPrice());
		assertEquals("Clean Code X", foundBook.getTitle());

		
	}
	

	
	@Test
	@Order(4)
	public void testDelete() throws JsonMappingException, JsonProcessingException {
		
		given().spec(specification)
		.contentType(TestConfigs.CONTENT_TYPE_XML)
		.accept(TestConfigs.CONTENT_TYPE_XML)
			.pathParam("id", book.getId())
			.when()
			.delete("{id}")
		.then()
			.statusCode(204);
		
	}

	@Test
	@Order(5)
	public void testFindAll() throws JsonMappingException, JsonProcessingException {		
		
		var content = given().spec(specification)
				.contentType(TestConfigs.CONTENT_TYPE_XML)
				.accept(TestConfigs.CONTENT_TYPE_XML)
				.queryParams("page", 1, "size", 5, "direction", "asc")
					.when()
					.get()
				.then()
					.statusCode(200)
						.extract()
						.body()
							.asString();
		
		PagedModelBook wrapper = objectMapper.readValue(content, PagedModelBook.class);
		List<BookVO> books = wrapper.getContent();
		
		BookVO foundBookOne = books.get(0);
		
		assertNotNull(foundBookOne.getId());
		assertNotNull(foundBookOne.getAuthor());
		assertNotNull(foundBookOne.getLaunchDate());
		assertNotNull(foundBookOne.getPrice());
		assertNotNull(foundBookOne.getTitle());
		
		assertEquals(11, foundBookOne.getId());
		
		assertEquals("Roger S. Pressman", foundBookOne.getAuthor());
		assertEquals(56.0, foundBookOne.getPrice());
		assertEquals("Engenharia de Software: uma abordagem profissional", foundBookOne.getTitle());
		
		BookVO foundBookFive = books.get(4);
		
		assertNotNull(foundBookFive.getId());
		assertNotNull(foundBookFive.getAuthor());
		assertNotNull(foundBookFive.getLaunchDate());
		assertNotNull(foundBookFive.getPrice());
		assertNotNull(foundBookFive.getTitle());
		
		assertEquals(4, foundBookFive.getId());
		
		assertEquals("Crockford", foundBookFive.getAuthor());
		assertEquals(67.0, foundBookFive.getPrice());
		assertEquals("JavaScript", foundBookFive.getTitle());
		
	}
	
	@Test
	@Order(6)
	public void testFindAllWitoutToken() throws JsonMappingException, JsonProcessingException {		
		
		RequestSpecification specificationWithoutToken = new RequestSpecBuilder()
				.setBasePath("/api/book/v1")
				.setPort(TestConfigs.SERVER_PORT)
					.addFilter(new RequestLoggingFilter(LogDetail.ALL))
					.addFilter(new ResponseLoggingFilter(LogDetail.ALL))
				.build();
		
			given().spec(specificationWithoutToken)
				.contentType(TestConfigs.CONTENT_TYPE_XML)
				.accept(TestConfigs.CONTENT_TYPE_XML)
				.when()
				.get()
				.then()
				.statusCode(403);
		
	}
	@Test
	@Order(7)
	public void testHATEOAS() throws JsonMappingException, JsonProcessingException {		
		
		var content = given().spec(specification)
				.contentType(TestConfigs.CONTENT_TYPE_XML)
				.accept(TestConfigs.CONTENT_TYPE_XML)
				.queryParams("page", 1, "size", 5, "direction", "asc")
					.when()
					.get()
				.then()
					.statusCode(200)
						.extract()
						.body()
							.asString();
		assertTrue(content.contains("<links><rel>self</rel><href>http://localhost:8081/api/book/v1/11</href></links>"));
		assertTrue(content.contains("<links><rel>self</rel><href>http://localhost:8081/api/book/v1/7</href></links>"));
		assertTrue(content.contains("<links><rel>self</rel><href>http://localhost:8081/api/book/v1/15</href></links>"));
		
		assertTrue(content.contains("<links><rel>first</rel><href>http://localhost:8081/api/book/v1?direction=asc&amp;page=0&amp;size=5&amp;sort=title,asc</href></links>"));
		assertTrue(content.contains("<links><rel>prev</rel><href>http://localhost:8081/api/book/v1?direction=asc&amp;page=0&amp;size=5&amp;sort=title,asc</href></links>"));
		assertTrue(content.contains("<links><rel>self</rel><href>http://localhost:8081/api/book/v1?page=1&amp;size=5&amp;direction=asc</href></links>"));
		assertTrue(content.contains("<links><rel>next</rel><href>http://localhost:8081/api/book/v1?direction=asc&amp;page=2&amp;size=5&amp;sort=title,asc</href></links>"));
		assertTrue(content.contains("<links><rel>last</rel><href>http://localhost:8081/api/book/v1?direction=asc&amp;page=2&amp;size=5&amp;sort=title,asc</href></links>"));
		assertTrue(content.contains("<page><size>5</size><totalElements>15</totalElements><totalPages>3</totalPages><number>1</number></page>"));
	
		
	}
	
	private void mockBook() {
		book.setAuthor("Robert C. Martin");
		book.setLaunchDate(new Date());;
		book.setPrice(77.00);
		book.setTitle("Clean Code");
		
	}

}

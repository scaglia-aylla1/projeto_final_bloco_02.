package com.generation.farmacia.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.generation.farmacia.model.Usuario;
import com.generation.farmacia.model.UsuarioLogin;
import com.generation.farmacia.repository.UsuarioRepository;
import com.generation.farmacia.service.UsuarioService;
import com.generation.farmacia.util.TestBuilder;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UsuarioControllerTest {
	
	@Autowired
	private TestRestTemplate testRestTemplate;

	@Autowired
	private UsuarioService usuarioService;
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	private static final String USUARIO_ROOT_EMAIL = "root@email.com";
	private static final String USUARIO_ROOT_SENHA = "rootroot";
	private static final String BASE_URL_USUARIOS = "/usuarios";
	
	@BeforeAll
	void start() {
		usuarioRepository.deleteAll();
		usuarioService.cadastrarUsuario(TestBuilder.criarUsuarioRoot());
	}
	
	@Test
	@DisplayName("Deve cadastrar um novo usuário com sucesso")
	public void deveCadastrarUsuario() {
		
		//Given
		Usuario usuario = TestBuilder.criarUsuario(null, "Renata Negrini", "renata_negrini@email.com.br", LocalDate.of(2005, 12, 12), "12345678");
		
		//When
		HttpEntity<Usuario> requisicao = new HttpEntity<Usuario>(usuario);
		ResponseEntity<Usuario> resposta = testRestTemplate.exchange(
				BASE_URL_USUARIOS + "/cadastrar", HttpMethod.POST, requisicao, Usuario.class);
		
		//Then
		assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
		assertEquals("Renata Negrini", resposta.getBody().getNome());
		assertEquals("renata_negrini@email.com.br", resposta.getBody().getUsuario());
		
	}
	
	@Test
	@DisplayName("Não deve permitir a duplicação do usuário")
	public void naoDeveDuplicarUsuario() {
		
		//Given
		Usuario usuario = TestBuilder.criarUsuario(null, "Angelo dos Santos","angelo@email.com.br",  LocalDate.of(2005, 12, 12), "12345678");
		usuarioService.cadastrarUsuario(usuario);
		
		// When
		HttpEntity<Usuario> requisicao = new HttpEntity<Usuario>(usuario);
		ResponseEntity<Usuario> resposta = testRestTemplate.exchange(
				BASE_URL_USUARIOS + "/cadastrar", HttpMethod.POST, requisicao, Usuario.class);
		
		// Then
		assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
		
	}
	@Test
	@DisplayName("Deve atualizar os dados de um usuário com sucesso")
	public void deveAtualizarUmUsuario() {
		
		//Given
		Usuario usuario = TestBuilder.criarUsuario(null, "Giovana Lucia Freitas", "giovana_lf@email.com.br", LocalDate.of(2005, 12, 12), "12345678");	
		Optional<Usuario> usuarioCadastrado = usuarioService.cadastrarUsuario(usuario);
		
		Usuario usuarioUpdate = TestBuilder.criarUsuario(usuarioCadastrado.get().getId(), "Giovana Lucia Freitas", "giovana_lf@email.com.br", LocalDate.of(2005, 12, 12), "12345678");
		
		//When
		HttpEntity<Usuario> requisicao = new HttpEntity<Usuario>(usuarioUpdate);
		ResponseEntity<Usuario> resposta = testRestTemplate
				.withBasicAuth(USUARIO_ROOT_EMAIL, USUARIO_ROOT_SENHA)
				.exchange(BASE_URL_USUARIOS + "/atualizar", HttpMethod.PUT, requisicao, Usuario.class);
		
		//Then
		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertEquals("Giovana Lucia Freitas", resposta.getBody().getNome());
		assertEquals("giovana_lf@email.com.br", resposta.getBody().getUsuario());
		
	}
	
	@Test
	@DisplayName("Deve listar todos os usuários com sucesso")
	public void deveListarTodosUsuarios() {
		
	
		//Given
		usuarioService.cadastrarUsuario(TestBuilder.criarUsuario(null, "Jovani Almeida", "jovani_almeida@email.com.br", LocalDate.of(2005, 12, 12), "12345678"));
		usuarioService.cadastrarUsuario(TestBuilder.criarUsuario(null, "Carlos Garcia", "carlos_garcia@email.com.br",  LocalDate.of(2005, 12, 12), "12345678"));
		
		//When
		ResponseEntity<Usuario[]> resposta = testRestTemplate
					.withBasicAuth(USUARIO_ROOT_EMAIL, USUARIO_ROOT_SENHA)
					.exchange(BASE_URL_USUARIOS + "/all", HttpMethod.GET, null, Usuario[].class);
		
		//Then
		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
	}
	
	@Test
	@DisplayName("Deve buscar o usuário por id")
	public void deveBuscarUsuarioPorId() {

	    // Given
	    Usuario usuario = TestBuilder.criarUsuario(null, "Ana Souza", "ana@gmail.com.br", LocalDate.of(2005, 12, 12), "12345678");
	    Optional<Usuario> usuarioCadastrado = usuarioService.cadastrarUsuario(usuario);

	    // When
	    ResponseEntity<Usuario> resposta = testRestTemplate
	            .withBasicAuth(USUARIO_ROOT_EMAIL, USUARIO_ROOT_SENHA)
	            .exchange(BASE_URL_USUARIOS + "/" + usuarioCadastrado.get().getId(), HttpMethod.GET, null, Usuario.class);

	    // Then
	    assertEquals(HttpStatus.OK, resposta.getStatusCode());
	    assertNotNull(resposta.getBody());
	    assertEquals("Ana Souza", resposta.getBody().getNome());
	    assertEquals("ana@gmail.com.br", resposta.getBody().getUsuario());
	}
	
	@Test
	@DisplayName("Deve autenticar o usuário com sucesso")
	public void deveAutenticarUsuarioComSucesso() {

	    // Given
	    Usuario usuario = TestBuilder.criarUsuario(null, "João Santos", "joao_santos@email.com.br",  LocalDate.of(1978, 7, 12), "senhaForte123");
	    usuarioService.cadastrarUsuario(usuario);

	    UsuarioLogin usuarioLogin = new UsuarioLogin("joao_santos@email.com.br", "senhaForte123");
	    HttpEntity<UsuarioLogin> requisicao = new HttpEntity<>(usuarioLogin);

	    // When
	    ResponseEntity<UsuarioLogin> resposta = testRestTemplate.exchange(
	            BASE_URL_USUARIOS + "/logar", HttpMethod.POST, requisicao, UsuarioLogin.class);

	    // Then
	    assertEquals(HttpStatus.OK, resposta.getStatusCode());
	    assertNotNull(resposta.getBody());
	    assertEquals("joao_santos@email.com.br", resposta.getBody().getUsuario());
	}
}
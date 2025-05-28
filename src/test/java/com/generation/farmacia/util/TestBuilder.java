package com.generation.farmacia.util;



import java.time.LocalDate;

import com.generation.farmacia.model.Usuario;

public class TestBuilder {

	public static Usuario criarUsuario(Long id, String nome, String email, LocalDate dataNascimento, String senha) {
	    Usuario usuario = new Usuario();
	    usuario.setId(id);
	    usuario.setNome(nome);
	    usuario.setUsuario(email);
	    usuario.setDataNascimento(dataNascimento);
	    usuario.setSenha(senha);
	    return usuario;
	}

	public static Usuario criarUsuarioRoot() {
	    return criarUsuario(null, "Root", "root@email.com", LocalDate.of(2000, 1, 1), "rootroot");
	}

}

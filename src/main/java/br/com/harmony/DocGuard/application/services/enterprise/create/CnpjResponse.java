package br.com.harmony.DocGuard.application.services.enterprise.create;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CnpjResponse {

    @JsonProperty("razao_social")
    private String razaoSocial;

    @JsonProperty("nome_fantasia")
    private String nomeFantasia;

    @JsonProperty("situacao_cadastral")
    private String situacaoCadastral;

    @JsonProperty("data_inicio_atividade")
    private LocalDate dataInicioAtividade;

    @JsonProperty("natureza_juridica")
    private String naturezaJuridica;

    @JsonProperty("porte_empresa")
    private String porteEmpresa;

    @JsonProperty("cnae_principal")
    private String cnaePrincipal;

    @JsonProperty("logradouro")
    private String logradouro;

    @JsonProperty("numero")
    private String numero;

    @JsonProperty("complemento")
    private String complemento;

    @JsonProperty("bairro")
    private String bairro;

    @JsonProperty("cep")
    private String cep;

    @JsonProperty("municipio")
    private String municipio;

    @JsonProperty("uf")
    private String uf;

    @JsonProperty("email")
    private String email;

    @JsonProperty("telefones")
    private List<TelefoneResponse> telefones;

    // pega o primeiro telefone disponível
    public String getPhone() {
        if (telefones == null || telefones.isEmpty()) return null;
        var t = telefones.get(0);
        return t.getDdd() + t.getNumero();
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TelefoneResponse {
        private String ddd;
        private String numero;
    }
}

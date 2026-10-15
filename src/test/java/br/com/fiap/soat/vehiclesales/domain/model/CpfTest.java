package br.com.fiap.soat.vehiclesales.domain.model;

import br.com.fiap.soat.vehiclesales.domain.exception.InvalidCpfException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfTest {

    @ParameterizedTest
    @ValueSource(strings = {"52998224725", "529.982.247-25", " 529 982 247 25 "})
    void acceptsValidCpfWithOrWithoutMask(String raw) {
        assertThat(new Cpf(raw).value()).isEqualTo("52998224725");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "123", "529982247250", "11111111111", "52998224726", "52998224715", "abc.def.ghi-jk"})
    void rejectsInvalidCpf(String raw) {
        assertThatThrownBy(() -> new Cpf(raw))
                .isInstanceOf(InvalidCpfException.class)
                .hasMessage("CPF inválido");
    }

    @Test
    void acceptsCpfWhoseCheckDigitRemainderIsTen() {
        // soma do 1º dígito = 12 -> (12 * 10) % 11 = 10, que vira 0
        assertThat(new Cpf("000.000.006-04").value()).isEqualTo("00000000604");
    }

    @Test
    void masksAllButTheMiddleDigits() {
        assertThat(new Cpf("52998224725").masked()).isEqualTo("***.982.247-**");
    }
}

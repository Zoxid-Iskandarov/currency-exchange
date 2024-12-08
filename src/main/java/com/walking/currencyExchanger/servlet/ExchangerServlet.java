package com.walking.currencyExchanger.servlet;

import com.walking.currencyExchanger.constant.ContextAttributeNames;
import com.walking.currencyExchanger.dto.ExchangeDto;
import com.walking.currencyExchanger.filter.ResponseJsonSerializerFilter;
import com.walking.currencyExchanger.repository.CurrencyRepository;
import com.walking.currencyExchanger.repository.ExchangeRateRepository;
import com.walking.currencyExchanger.validator.ParameterValidator;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

public class ExchangerServlet extends HttpServlet {
    private ExchangeRateRepository exchangeRateRepository;
    private CurrencyRepository currencyRepository;

    @Override
    public void init(ServletConfig config) throws ServletException {
        var servletContext = config.getServletContext();

        this.exchangeRateRepository = (ExchangeRateRepository) servletContext.getAttribute(
                ContextAttributeNames.EXCHANGE_RATE_REPOSITORY);
        this.currencyRepository = (CurrencyRepository) servletContext.getAttribute(
                ContextAttributeNames.CURRENCY_REPOSITORY);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            var from = req.getParameter("from");
            var to = req.getParameter("to");
            var amount = new BigDecimal(req.getParameter("amount"));

            if (ParameterValidator.checkExchangeParameter(from, to, amount)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Данные введены неправильно. Пример: .../exchange?from=USD&to=RUB&amount=10");
                return;
            }

            var fromCurrency = currencyRepository.findByCode(from);
            var toCurrency = currencyRepository.findByCode(to);

            if (fromCurrency.isEmpty() || toCurrency.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Валюта не найдена");
                return;
            }

            var rate = getRate(from, to);

            if (rate == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Обменный курс не найден");
                return;
            }

            var exchangeDto = ExchangeDto.builder()
                    .baseCurrency(fromCurrency.get())
                    .targetCurrency(toCurrency.get())
                    .rate(rate)
                    .amount(amount)
                    .converterAmount(rate.multiply(amount))
                    .build();

            req.setAttribute(ResponseJsonSerializerFilter.POJO_RESPONSE_BODY, exchangeDto);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Данные введены неправильно. Пример: Пример: .../exchange?from=USD&to=RUB&amount=10");
        }
    }

    private BigDecimal getRate(String from, String to) {
        var exchangeRate = exchangeRateRepository.findByCodes(from, to);

        if (exchangeRate.isPresent()) {
            return exchangeRate.get().getRate();
        }

        var reverseExchangeRate = exchangeRateRepository.findByCodes(to, from);

        if (reverseExchangeRate.isPresent()) {
            return reverseExchangeRate.get().getRate();
        }

        var exchangeRateUSD_A = exchangeRateRepository.findByCodes("USD", from);
        var exchangeRateUSD_B = exchangeRateRepository.findByCodes("USD", to);

        if (exchangeRateUSD_A.isPresent() && exchangeRateUSD_B.isPresent()) {
            return exchangeRateUSD_A.get().getRate().divide(exchangeRateUSD_B.get().getRate());
        }

        return null;
    }
}

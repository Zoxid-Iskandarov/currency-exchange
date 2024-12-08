package com.walking.currencyExchanger.servlet;

import com.walking.currencyExchanger.constant.ContextAttributeNames;
import com.walking.currencyExchanger.entity.ExchangeRate;
import com.walking.currencyExchanger.filter.ResponseJsonSerializerFilter;
import com.walking.currencyExchanger.mapper.ExchangeRateDtoMapper;
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

public class ExchangeRateManagementServlet extends HttpServlet {
    private ExchangeRateRepository repository;
    private CurrencyRepository currencyRepository;
    private ExchangeRateDtoMapper mapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        var servletContext = config.getServletContext();

        this.repository = (ExchangeRateRepository) servletContext.getAttribute(
                ContextAttributeNames.EXCHANGE_RATE_REPOSITORY);
        this.currencyRepository = (CurrencyRepository) servletContext.getAttribute(
                ContextAttributeNames.CURRENCY_REPOSITORY);

        this.mapper = (ExchangeRateDtoMapper) servletContext.getAttribute(ContextAttributeNames.EXCHANGE_RATE_DTO_MAPPER);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var exchangeRates = repository.findAll();

        var exchangeRateList = exchangeRates.stream()
                .map(mapper::map)
                .toList();

        req.setAttribute(ResponseJsonSerializerFilter.POJO_RESPONSE_BODY, exchangeRateList);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            var baseCurrencyCode = req.getParameter("baseCurrencyCode");
            var targetCurrencyCode = req.getParameter("targetCurrencyCode");
            var rate = new BigDecimal(req.getParameter("rate"));

            if (ParameterValidator.checkExchangeParameter(baseCurrencyCode, targetCurrencyCode, rate)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Не правильно введены данные. Пример: baseCurrencyCode = 'USD', targetCurrencyCode = 'EUR', rate = '0.99'");
                return;
            }

            var baseCurrency = currencyRepository.findByCode(baseCurrencyCode);
            var targetCurrency = currencyRepository.findByCode(targetCurrencyCode);

            if (baseCurrency.isEmpty() || targetCurrency.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Валюта не найдена");
                return;
            }

            var exchangeRate = repository.findByCodes(baseCurrencyCode, targetCurrencyCode);

            if (exchangeRate.isPresent()) {
                resp.sendError(HttpServletResponse.SC_CONFLICT, "Обменный курс для данных кодов уже существует");
                return;
            }

            ExchangeRate newExchangeRate = ExchangeRate.builder()
                    .baseCurrencyId(baseCurrency.get().getId())
                    .baseCurrency(baseCurrency.get())
                    .targetCurrencyId(targetCurrency.get().getId())
                    .targetCurrency(targetCurrency.get())
                    .rate(rate)
                    .build();

            var createExchangeRate = repository.save(newExchangeRate);

            req.setAttribute(ResponseJsonSerializerFilter.POJO_RESPONSE_BODY, mapper.map(createExchangeRate));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Обменный курс не правильно введен. Пример: baseCurrencyCode = 'USD', targetCurrencyCode = 'EUR', rate = '0.99'");
        }
    }
}

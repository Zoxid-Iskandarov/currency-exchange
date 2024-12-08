package com.walking.currencyExchanger.servlet;

import com.walking.currencyExchanger.constant.ContextAttributeNames;
import com.walking.currencyExchanger.entity.ExchangeRate;
import com.walking.currencyExchanger.filter.ResponseJsonSerializerFilter;
import com.walking.currencyExchanger.mapper.ExchangeRateDtoMapper;
import com.walking.currencyExchanger.repository.ExchangeRateRepository;
import com.walking.currencyExchanger.validator.ParameterValidator;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

public class ExchangeRateInfoServlet extends HttpServlet {
    private ExchangeRateRepository repository;
    private ExchangeRateDtoMapper mapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        var servletContext = config.getServletContext();

        this.repository = (ExchangeRateRepository) servletContext.getAttribute(
                ContextAttributeNames.EXCHANGE_RATE_REPOSITORY);
        this.mapper = (ExchangeRateDtoMapper) servletContext.getAttribute(
                ContextAttributeNames.EXCHANGE_RATE_DTO_MAPPER);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var exchangeRate = getExchangeRateFromRequest(req, resp);

        if (exchangeRate == null) {
            return;
        }

        var exchangeRateDto = mapper.map(exchangeRate);

        req.setAttribute(ResponseJsonSerializerFilter.POJO_RESPONSE_BODY, exchangeRateDto);
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var exchangeRate = getExchangeRateFromRequest(req, resp);

        if (exchangeRate == null) {
            return;
        }

        try {
            var rate = new BigDecimal(req.getParameter("rate"));

            exchangeRate.setRate(rate);
            var updatedExchangeRate = repository.update(exchangeRate);

            req.setAttribute(ResponseJsonSerializerFilter.POJO_RESPONSE_BODY, mapper.map(updatedExchangeRate));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Неправильно введены данные. Пример: rate = '0.73'");
        }
    }

    private ExchangeRate getExchangeRateFromRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Неправильно введен обменный курс. Пример: .../exchangeRate/USDRUB");
            return null;
        }

        var currencyCodes = pathInfo.substring(1).toUpperCase();

        if (ParameterValidator.checkExchangeParameter(currencyCodes)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Неправильно введена пара валют. Пример: .../exchangeRate/USDRUB");
            return null;
        }

        var baseCurrencyCode = currencyCodes.substring(0, 3);
        var targetCurrencyCode = currencyCodes.substring(3, 6);

        var exchangeRate = repository.findByCodes(baseCurrencyCode, targetCurrencyCode);

        if (exchangeRate.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Обменный курс для данных кодов не найден");
            return null;
        }

        return exchangeRate.get();
    }
}

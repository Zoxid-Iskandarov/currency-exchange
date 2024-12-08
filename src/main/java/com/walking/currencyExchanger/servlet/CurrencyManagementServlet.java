package com.walking.currencyExchanger.servlet;

import com.walking.currencyExchanger.constant.ContextAttributeNames;
import com.walking.currencyExchanger.entity.Currency;
import com.walking.currencyExchanger.filter.ResponseJsonSerializerFilter;
import com.walking.currencyExchanger.repository.CurrencyRepository;
import com.walking.currencyExchanger.validator.ParameterValidator;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CurrencyManagementServlet extends HttpServlet {
    private CurrencyRepository repository;

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.repository = (CurrencyRepository) config.getServletContext().getAttribute(
                ContextAttributeNames.CURRENCY_REPOSITORY);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute(ResponseJsonSerializerFilter.POJO_RESPONSE_BODY, repository.findAll());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var code = req.getParameter("code");
        var name = req.getParameter("name");
        var sign = req.getParameter("sign");

        if (ParameterValidator.checkCurrencyParameter(code, name, sign)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Валюта введена неправильно. Пример: code = 'USD', name = 'United States Dollar', sign = '$'");
            return;
        }

        if (repository.findByCode(code).isPresent()) {
            resp.sendError(HttpServletResponse.SC_CONFLICT, "Данная валюты уже сущестувет");
            return;
        }

        Currency currency = Currency.builder()
                .code(code)
                .name(name)
                .sign(sign)
                .build();

        Currency createdCurrency = repository.save(currency);

        req.setAttribute(ResponseJsonSerializerFilter.POJO_RESPONSE_BODY, createdCurrency);
    }
}

package com.walking.currencyExchanger.servlet;

import com.walking.currencyExchanger.constant.ContextAttributeNames;
import com.walking.currencyExchanger.filter.ResponseJsonSerializerFilter;
import com.walking.currencyExchanger.repository.CurrencyRepository;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CurrencyInfoServlet extends HttpServlet {
    private CurrencyRepository repository;

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.repository = (CurrencyRepository) config.getServletContext().getAttribute(
                ContextAttributeNames.CURRENCY_REPOSITORY);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Неправильно введена валюты. Пример .../currency/EUR");
            return;
        }

        var currencyCode = pathInfo.substring(1).toUpperCase();

        var currency = repository.findByCode(currencyCode);

        if (currency.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Валюта не найдена");
            return;
        }

        req.setAttribute(ResponseJsonSerializerFilter.POJO_RESPONSE_BODY, currency.get());
    }
}

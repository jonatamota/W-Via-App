#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Scraper da base pública de médicos do CFM (portal.cfm.org.br/busca-medicos).

Uso:
    python scraper_cfm.py --ufs "SP RJ" [--cidades cidades.txt] [--atraso 2.5] [--saida resultado]

Requisitos:
    Python 3.8+, Chrome instalado, `pip install -r requirements.txt`.

Comportamento:
  * Abre o Chrome com janela visível e perfil persistente, para que o
    reCAPTCHA seja resolvido manualmente UMA vez; a sessão é reutilizada.
  * Itera UF -> municípios, busca e pagina os resultados, salvando um CSV
    por município em `<saida>/<UF>/<municipio>.csv` (UTF-8 com BOM).
  * Se o reCAPTCHA reaparecer no meio, pausa e aguarda a resolução manual.
  * Pula municípios já salvos (resume): pode interromper com Ctrl+C e rodar
    de novo para continuar de onde parou.
"""

import argparse
import logging
import os
import random
import time

import pandas as pd
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import Select, WebDriverWait
from webdriver_manager.chrome import ChromeDriverManager

URL_BUSCA = "https://portal.cfm.org.br/busca-medicos"


def configurar_logger():
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s | %(levelname)s | %(message)s",
        handlers=[
            logging.FileHandler("scraper.log", encoding="utf-8"),
            logging.StreamHandler(),
        ],
    )
    return logging.getLogger("scraper_cfm")


def inicializar_driver():
    service = Service(ChromeDriverManager().install())
    options = webdriver.ChromeOptions()
    options.add_argument("--start-maximized")
    options.add_argument("--disable-blink-features=AutomationControlled")
    options.add_argument(
        "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"
    )
    options.add_experimental_option("excludeSwitches", ["enable-automation"])
    options.add_experimental_option("useAutomationExtension", False)
    options.add_argument(
        "user-data-dir=" + os.path.abspath("chrome_profile")
    )
    driver = webdriver.Chrome(service=service, options=options)
    driver.execute_cdp_cmd("Page.addScriptToEvaluateOnNewDocument", {
        "source": "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})"
    })
    return driver


def fechar_popup_cookies(driver):
    wait = WebDriverWait(driver, 5)
    try:
        botao = wait.until(EC.element_to_be_clickable(
            (By.XPATH, '//button[contains(text(), "Aceito")]')))
        botao.click()
        time.sleep(random.uniform(1.0, 2.5))
    except Exception:
        pass


def resolver_captcha_se_necessario(driver, logger):
    """Se houver reCAPTCHA na tela, pausa até o usuário resolver na janela."""
    iframe_selector = "iframe[src*='recaptcha'], iframe[title='reCAPTCHA']"
    try:
        caixa = driver.find_element(By.CSS_SELECTOR, "div#recaptcha-anchor")
    except Exception:
        caixa = None

    if caixa is not None and caixa.get_attribute("aria-checked") == "true":
        return

    presentes = driver.find_elements(By.CSS_SELECTOR, iframe_selector)
    if not presentes and caixa is None:
        return

    logger.warning(
        ">> ReCAPTCHA detectado. RESOLVA NA JANELA DO CHROME e aguarde... "
        "(não feche o navegador)"
    )
    while True:
        time.sleep(2.5)
        try:
            nova = driver.find_element(By.CSS_SELECTOR, "div#recaptcha-anchor")
            if nova.get_attribute("aria-checked") == "true":
                logger.info("ReCAPTCHA resolvido. Continuando...")
                time.sleep(1.5)
                return
        except Exception:
            if not driver.find_elements(By.CSS_SELECTOR, iframe_selector):
                logger.info("ReCAPTCHA não está mais presente. Continuando...")
                return


def selecionar_municipio(driver, wait, municipio_valor):
    select_municipio = Select(driver.find_element(By.ID, "municipio"))
    select_municipio.select_by_value(municipio_valor)
    return select_municipio.first_selected_option.text.strip()


def clicar_buscar(driver, wait):
    botao = driver.find_element(
        By.XPATH,
        "//*[@id='buscaForm']//button[contains(normalize-space(.), 'Buscar')]",
    )
    driver.execute_script("arguments[0].scrollIntoView(true);", botao)
    time.sleep(random.uniform(0.4, 1.0))
    wait.until(EC.element_to_be_clickable(botao)).click()


def ha_resultados(driver):
    try:
        alerta = driver.find_element(
            By.XPATH,
            "//*[@id='content']//p[contains(text(), 'Nenhum resultado encontrado')]",
        )
        return not alerta.is_displayed()
    except Exception:
        return True


def coletar_cards(driver, wait):
    wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, ".card-body")))
    medicos = []
    pagina = 1
    while True:
        cards = driver.find_elements(By.CSS_SELECTOR, ".card-body")
        if not cards:
            break
        for card in cards:
            def texto(padrao):
                try:
                    return card.find_element(
                        By.XPATH,
                        f".//div[contains(@class,'row')]//div[contains(.,'{padrao}')]",
                    ).text.replace(padrao, "").strip()
                except Exception:
                    return ""
            try:
                nome = card.find_element(By.TAG_NAME, "h4").text.strip()
            except Exception:
                nome = ""
            medicos.append({
                "Nome": nome,
                "CRM": texto("CRM:"),
                "Inscricao": texto("Inscrição:"),
                "Situacao": texto("Situação:"),
                "Especialidades": texto("Especialidades/Áreas de Atuação:"),
            })

        # próxima página via paginationjs (data-num) ou botão next
        try:
            prox = pagina + 1
            link = driver.find_element(
                By.XPATH,
                f"//li[contains(@class,'paginationjs-page') and @data-num='{prox}']/a",
            )
            driver.execute_script("arguments[0].click();", link)
            pagina += 1
            time.sleep(random.uniform(1.8, 3.5))
        except Exception:
            try:
                prox = driver.find_element(
                    By.CSS_SELECTOR, ".paginationjs-next a")
                if "disabled" in (prox.get_attribute("class") or ""):
                    break
                driver.execute_script("arguments[0].click();", prox)
                pagina += 1
                time.sleep(random.uniform(1.8, 3.5))
            except Exception:
                break
    return medicos


def salvar_csv(caminho, linhas):
    os.makedirs(os.path.dirname(caminho), exist_ok=True)
    pd.DataFrame(linhas).to_csv(caminho, index=False, encoding="utf-8-sig")


def buscar_uf(driver, logger, uf, cidades_filtro, atraso, saida):
    wait = WebDriverWait(driver, 12)
    driver.get(URL_BUSCA)
    time.sleep(random.uniform(2, 4))
    fechar_popup_cookies(driver)
    resolver_captcha_se_necessario(driver, logger)

    wait.until(EC.presence_of_element_located((By.ID, "uf")))
    Select(driver.find_element(By.ID, "uf")).select_by_value(uf)
    time.sleep(random.uniform(1.5, 3.0))

    wait.until(EC.presence_of_element_located((By.ID, "municipio")))
    select_municipio = Select(driver.find_element(By.ID, "municipio"))
    municipios = [
        (opt.get_attribute("value"), opt.text.strip())
        for opt in select_municipio.options
        if opt.get_attribute("value")
    ]
    if cidades_filtro:
        municipios = [
            (v, n) for v, n in municipios
            if n.casefold() in cidades_filtro
        ]
    logger.info("%s - municípios a processar: %d", uf, len(municipios))

    pasta_uf = os.path.join(saida, uf)
    for valor, nome in municipios:
        slug = nome.replace(" ", "_").replace("/", "-").replace("__", "_")
        caminho = os.path.join(pasta_uf, f"{slug}.csv")
        if os.path.exists(caminho):
            logger.info("%s - %s: já salvo, pulando", uf, nome)
            continue
        try:
            selecionar_municipio(driver, wait, valor)
            clicar_buscar(driver, wait)
            time.sleep(random.uniform(atraso, atraso + 1.5))
            resolver_captcha_se_necessario(driver, logger)

            if not ha_resultados(driver):
                logger.info("%s - %s: nenhum resultado", uf, nome)
                salvar_csv(caminho, [])
                driver.get(URL_BUSCA)
                time.sleep(random.uniform(1.5, 3.0))
                wait.until(EC.presence_of_element_located((By.ID, "uf")))
                Select(driver.find_element(By.ID, "uf")).select_by_value(uf)
                continue

            medicos = coletar_cards(driver, wait)
            logger.info("%s - %s: %d médicos", uf, nome, len(medicos))
            salvar_csv(caminho, medicos)
        except Exception as e:
            logger.error("%s - %s: erro: %s", uf, nome, e)
            continue

        driver.get(URL_BUSCA)
        time.sleep(random.uniform(1.5, 3.0))
        wait.until(EC.presence_of_element_located((By.ID, "uf")))
        Select(driver.find_element(By.ID, "uf")).select_by_value(uf)


def main():
    parser = argparse.ArgumentParser(description="Scraper da base de médicos do CFM")
    parser.add_argument("--ufs", required=True,
                        help="Siglas das UFs separadas por espaço, ex.: \"SP RJ\"")
    parser.add_argument("--cidades", default=None,
                        help="Arquivo .txt com uma cidade por linha para restringir")
    parser.add_argument("--atraso", type=float, default=2.5,
                        help="Atraso base entre consultas em segundos")
    parser.add_argument("--saida", default="resultado",
                        help="Pasta de saída dos CSVs")
    args = parser.parse_args()

    logger = configurar_logger()
    ufs = [u.strip().upper() for u in args.ufs.split() if u.strip()]
    cidades_filtro = None
    if args.cidades:
        with open(args.cidades, encoding="utf-8") as f:
            cidades_filtro = {linha.strip().casefold() for linha in f if linha.strip()}

    driver = inicializar_driver()
    try:
        for uf in ufs:
            logger.info("=== Iniciando UF %s ===", uf)
            buscar_uf(driver, logger, uf, cidades_filtro, args.atraso, args.saida)
    finally:
        driver.quit()
    logger.info("Extração finalizada. Envie a pasta %s para o desenvolvimento.", args.saida)


if __name__ == "__main__":
    main()

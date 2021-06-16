--
-- PostgreSQL database dump
--

-- Dumped from database version 12.5
-- Dumped by pg_dump version 12.5

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: cdr; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cdr (
    cdr_id integer NOT NULL,
    cdr_li_id integer,
    processed boolean,
    cdr_timestamp timestamp without time zone,
    original_file_path character varying(100),
    converted_file_path character varying(100)
);


ALTER TABLE public.cdr OWNER TO postgres;

--
-- Name: cdr_location_info; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cdr_location_info (
    cdr_li_id integer NOT NULL,
    filepath character varying(100),
    server_id integer,
    type character varying(30)
);


ALTER TABLE public.cdr_location_info OWNER TO postgres;

--
-- Name: rules; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.rules (
    source_cdr_li_id integer,
    destination_cdr_li_id integer,
    rules_id integer NOT NULL,
    period_value character varying(40),
    period_unit character varying(40)
);


ALTER TABLE public.rules OWNER TO postgres;

--
-- Name: server; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.server (
    server_id integer NOT NULL,
    name character varying(20),
    description character varying(100),
    ip integer,
    port integer,
    protocol character varying(20),
    username character varying(30),
    password character varying(30)
);


ALTER TABLE public.server OWNER TO postgres;

--
-- Data for Name: cdr; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cdr (cdr_id, cdr_li_id, processed, cdr_timestamp, original_file_path, converted_file_path) FROM stdin;
\.


--
-- Data for Name: cdr_location_info; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cdr_location_info (cdr_li_id, filepath, server_id, type) FROM stdin;
\.


--
-- Data for Name: rules; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.rules (source_cdr_li_id, destination_cdr_li_id, rules_id, period_value, period_unit) FROM stdin;
\.


--
-- Data for Name: server; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.server (server_id, name, description, ip, port, protocol, username, password) FROM stdin;
\.


--
-- Name: cdr_location_info cdr_location_info_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cdr_location_info
    ADD CONSTRAINT cdr_location_info_pkey PRIMARY KEY (cdr_li_id);


--
-- Name: cdr cdr_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cdr
    ADD CONSTRAINT cdr_pkey PRIMARY KEY (cdr_id);


--
-- Name: rules rules_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rules
    ADD CONSTRAINT rules_pkey PRIMARY KEY (rules_id);


--
-- Name: server server_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server
    ADD CONSTRAINT server_name_key UNIQUE (name);


--
-- Name: server server_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.server
    ADD CONSTRAINT server_pkey PRIMARY KEY (server_id);


--
-- Name: cdr cdr_cdr_li_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cdr
    ADD CONSTRAINT cdr_cdr_li_id_fkey FOREIGN KEY (cdr_li_id) REFERENCES public.cdr_location_info(cdr_li_id);


--
-- Name: cdr_location_info cdr_location_info_server_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cdr_location_info
    ADD CONSTRAINT cdr_location_info_server_id_fkey FOREIGN KEY (server_id) REFERENCES public.server(server_id);


--
-- Name: rules rules_destination_cdr_li_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rules
    ADD CONSTRAINT rules_destination_cdr_li_id_fkey FOREIGN KEY (destination_cdr_li_id) REFERENCES public.cdr_location_info(cdr_li_id);


--
-- Name: rules rules_source_cdr_li_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rules
    ADD CONSTRAINT rules_source_cdr_li_id_fkey FOREIGN KEY (source_cdr_li_id) REFERENCES public.cdr_location_info(cdr_li_id);


--
-- PostgreSQL database dump complete
--


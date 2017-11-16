CREATE OR REPLACE PACKAGE content_search IS
        FUNCTION get_snippet(indexName VARCHAR2, ids VARCHAR2, query VARCHAR2, startTag VARCHAR2, endTag VARCHAR2, translation VARCHAR2, separator VARCHAR2) RETURN sys_refcursor;

	FUNCTION check_flag(flags NUMBER, flag NUMBER) RETURN BOOLEAN;

$if DBMS_DB_VERSION.VER_LE_10 = false $then
	PROCEDURE search_with_resultsetinterface(index_name in VARCHAR2,
		table_name in VARCHAR2,
		query in VARCHAR2,
		field_list in VARCHAR2,
		result_set_descriptor IN CLOB,
		text_result OUT NOCOPY CLOB,
		meta_result OUT sys_refCURSOR,
		additional_result OUT sys_refcursor,
		rcount OUT INT,
		start_tag IN VARCHAR2,
		end_tag IN VARCHAR2,
		translation IN VARCHAR2,
		separator IN VARCHAR2,
		flags IN INT,
		trace OUT NOCOPY CLOB);
    
    PROCEDURE hays_search(index_name in VARCHAR2,
		table_name in VARCHAR2,
		query in VARCHAR2,
		field_list in VARCHAR2,
		result_set_descriptor IN CLOB,
		text_result OUT NOCOPY CLOB,
		meta_result OUT sys_refCURSOR,
		additional_result OUT sys_refcursor,
		rcount OUT INT,
		start_tag IN VARCHAR2,
		end_tag IN VARCHAR2,
		translation IN VARCHAR2,
		separator IN VARCHAR2,
		flags IN INT,
		trace OUT NOCOPY CLOB,
    ne_latitude NUMBER,
    ne_longitude NUMBER,
    sw_latitude NUMBER,
    sw_longitude NUMBER, 
    radius NUMBER, 
    exclude_equals_job_locations INTEGER DEFAULT 2);
    
$end
END ;

/


CREATE OR REPLACE PACKAGE BODY content_search AS
        
	FUNCTION get_snippet(indexName VARCHAR2, ids VARCHAR2, query VARCHAR2, startTag VARCHAR2, endTag VARCHAR2, translation VARCHAR2, separator VARCHAR2) RETURN sys_refcursor IS
        snippet content_SNIPPETS;
        endIndex INTEGER := 1;
        startIndex INTEGER := 1;
        len INTEGER := 0;
        id VARCHAR2(300);
        tranBool BOOLEAN := FALSE;
        result sys_refcursor;
        BEGIN
                IF UPPER(translation) = 'TRUE' THEN
                        tranBool := TRUE;
                ELSE
                        tranBool := FALSE;
                END IF;
                snippet := content_SNIPPETS();
                LOOP
                        endIndex := INSTR(ids, ',', startIndex);
                        IF endIndex = 0 THEN
                                len := LENGTH(ids) - startIndex + 1;
                        ELSE
                                len := endIndex - startIndex;
                        END IF;

                        id := SUBSTR(ids, startIndex, len);
                        startIndex := endIndex + 1;
                        snippet.extend(1);
                        snippet(snippet.LAST) := CTX_DOC.SNIPPET(indexName, id, query, startTag, endTag, tranBool, separator);
                        EXIT WHEN endIndex = 0;
                END LOOP;
                OPEN result FOR SELECT * FROM TABLE(CAST(snippet AS content_SNIPPETS));
                RETURN result;
        END;

	FUNCTION check_flag(flags NUMBER, flag NUMBER) RETURN BOOLEAN IS
	flags_raw	RAW(100);
	flag_raw	RAW(100);
	
	BEGIN		
		IF bitand(flags, flag) = flag THEN
			RETURN TRUE;
		END IF;
		RETURN FALSE;
	END;

$if DBMS_DB_VERSION.VER_LE_10 = false $then
	PROCEDURE search_with_resultsetinterface(index_name in VARCHAR2,
		table_name in VARCHAR2,
		query in VARCHAR2,
		field_list in VARCHAR2,
		result_set_descriptor IN CLOB,
		text_result OUT NOCOPY CLOB,
		meta_result OUT sys_refCURSOR,
		additional_result OUT sys_refcursor,
		rcount OUT INT,
		start_tag IN VARCHAR2,
		end_tag IN VARCHAR2,
		translation IN VARCHAR2,
		separator IN VARCHAR2,
		flags IN INT,
		trace OUT NOCOPY CLOB) AS

	tp	DBMS_XMLPARSER.PARSER := dbms_xmlParser.newparser();
	doc	DBMS_XMLDOM.DOMDocument;
	node	DBMS_XMLDOM.DOMNODE;
	tmpNode DBMS_XMLDOM.DOMNODE;
	nl	DBMS_XMLDOM.DOMNODELIST;
	hit	DBMS_XMLDOM.DOMNODE;
	field	DBMS_XMLDOM.DOMNODE;
	hl	DBMS_XMLDOM.DOMNODELIST;
	fl	DBMS_XMLDOM.DOMNODELIST;
	flen	PLS_INTEGER;
	name	VARCHAR2 (100);
	len	INT;
	hlen	PLS_INTEGER;
	i	PLS_INTEGER;
	j	PLS_INTEGER;
	tr	search_result_row;
	tt	search_result_collection  := search_result_collection();
	nnm	DBMS_XMLDOM.DOMNAMEDNODEMAP;
	attr	DBMS_XMLDOM.DOMNODE;
	text	DBMS_XMLDOM.DOMNODE;

  cursor hays_locations is 
  select l.location_id from rdfusr.hays_locations l
  where sdo_inside(l.geo_location, SDO_GEOMETRY(2003,  8307, NULL, SDO_ELEM_INFO_ARRAY(1,1003,3), SDO_ORDINATE_ARRAY(-0.166,51.4218,0.11345, 51.662))) =  'TRUE'
  --and rownum < 11
  ;
  
  hays_var_location rdfusr.hays_locations.location_id%type; 

	mq	CLOB; 
	tmpLob CLOB;
	isFirst	BOOLEAN := TRUE;
	ns	BOOLEAN := TRUE;
	mqlc	PLS_INTEGER := 1;
        
        
	tranBool BOOLEAN := FALSE;

	dID	INT;
	docName	VARCHAR2 (100);
	score	INT;
	snippet	VARCHAR2 (4000);

	isDebug	BOOLEAN := FALSE;
	useSnippet	BOOLEAN := FALSE;
	dcount	VARCHAR(100);
	dl	DBMS_XMLDOM.DOMNODELIST;
	dValue	VARCHAR2 (1000);
  hays_var_all_locations varchar(32767);
  hays_query varchar(32767);
	BEGIN
		isDebug := true;--check_flag(flags, 1);
		useSnippet := check_flag(flags, 2);

		dbms_lob.createtemporary(tmpLob, true, DBMS_LOB.CALL);
		dbms_lob.createtemporary(text_result, true, DBMS_LOB.CALL);
		IF useSnippet THEN
			DBMS_LOB.WRITEAPPEND(tmpLob,  17, ' Snippet enabled.');
		END IF;
		IF isDebug THEN
			DBMS_LOB.WRITEAPPEND(tmpLob,  1, CHR(13));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1, CHR(10));
			DBMS_LOB.WRITEAPPEND(tmpLob,  33, 'ctx_query.result_set start time: ');
			DBMS_LOB.WRITEAPPEND(tmpLob,  11, to_char(systimestamp, 'HH24:MI:SSxFF'));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1, CHR(13));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1, CHR(10));
		END IF;
     hays_query:=query;
    /*
    hays_var_all_locations := '';
    open hays_locations;
    Loop
       FETCH hays_locations INTO hays_var_location;
        Exit when hays_locations%notfound;
        hays_var_all_locations := hays_var_all_locations || ' or ' || hays_var_location;
     end loop;   */
    
    IF isDebug THEN
      DBMS_LOB.WRITEAPPEND(tmpLob,  18, 'spatial end time: ');
      DBMS_LOB.WRITEAPPEND(tmpLob,  11, to_char(systimestamp, 'HH24:MI:SSxFF'));
      --DBMS_LOB.WRITEAPPEND(tmpLob,  24, 'all locations variable: ');
			--DBMS_LOB.WRITEAPPEND(tmpLob,  length(hays_var_all_locations), hays_var_all_locations);  
    end if;
    
    /*
    if length(query) = 0 then
     hays_query:=query;
     -- hays_query := '((' || substr(hays_var_all_locations, 4) || ') WITHIN xLocation)';
    else
     hays_query:=query;
     --hays_query := substr( query, 0, length(query)-1)|| ' and ((' || substr(hays_var_all_locations, 4) || ') WITHIN xLocation))';
    end if;
    
    IF isDebug THEN
      DBMS_LOB.WRITEAPPEND(tmpLob,  17, 'hays full query: ');
			DBMS_LOB.WRITEAPPEND(tmpLob,  length(hays_query), hays_query);  
    end if;*/
     
		ctx_query.result_set(index_name, hays_query, result_set_descriptor, text_result);

		IF isDebug THEN
			DBMS_LOB.WRITEAPPEND(tmpLob,  33, 'ctx_query.result_set   end time: ');  
			DBMS_LOB.WRITEAPPEND(tmpLob,  11, to_char(systimestamp, 'HH24:MI:SSxFF'));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(13));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(10));
		END IF;		
		DBMS_XMLPARSER.PARSECLOB(tp, text_result);
		doc := DBMS_XMLPARSER.GETDOCUMENT(tp);
		tmpNode := DBMS_XMLDOM.MAKENODE(doc);

--		 This is count;
		node := DBMS_XMLDOM.GETFIRSTCHILD(tmpNode);
		DBMS_XMLDOM.FREENODE(tmpNode);
		
		nl := DBMS_XMLDOM.GETCHILDNODES(node);
		len := DBMS_XMLDOM.GETLENGTH(nl);	
		DBMS_XMLDOM.FREENODE(node);

		IF isDebug THEN
            DBMS_LOB.WRITEAPPEND(tmpLob,  27, 'Result parsing start time: ');                 	
			DBMS_LOB.WRITEAPPEND(tmpLob,  11, to_char(systimestamp, 'HH24:MI:SSxFF'));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(13));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(10));
		END IF;

		FOR i IN 0..len - 1 LOOP
			node := DBMS_XMLDOM.ITEM(nl, i);
			name := DBMS_XMLDOM.GETNODENAME(node);	

			IF name = 'count' THEN
--				dbms_output.put_line('found count node');
				text := DBMS_XMLDOM.GETFIRSTCHILD(node);
				rcount := DBMS_XMLDOM.GETNODEVALUE(text);	
				DBMS_XMLDOM.FREENODE(text);
			ELSE IF name = 'hitlist' THEN

				hl := DBMS_XMLDOM.GETCHILDNODES(node);
				hlen := DBMS_XMLDOM.GETLENGTH(hl);
				tt.extend(hlen);

				FOR j IN 0..hlen - 1 LOOP
					hit := DBMS_XMLDOM.ITEM(hl, j);
					name := DBMS_XMLDOM.GETNODENAME(hit);
					IF name = 'hit' THEN
						fl := DBMS_XMLDOM.GETCHILDNODES(hit);
						flen := DBMS_XMLDOM.GETLENGTH(fl);
						dID := -1;
						score := -1;
						docName := '';
						snippet := '';
						FOR k IN 0..flen - 1 LOOP
						
							field := DBMS_XMLDOM.ITEM(fl, k);							
							
							nnm := DBMS_XMLDOM.GETATTRIBUTES(field);
							attr := DBMS_XMLDOM.GETNAMEDitem(nnm, 'name');
							name := DBMS_XMLDOM.GETNODEVALUE(attr);
							
							IF name = 'DID' THEN
								text := DBMS_XMLDOM.GETFIRSTCHILD(field);
								dID := DBMS_XMLDOM.GETNODEVALUE(text);
								DBMS_XMLDOM.FREENODE(text);
							END IF;
							IF name = 'SDDDOCNAME' THEN
								text := DBMS_XMLDOM.GETFIRSTCHILD(field);
								docName := DBMS_XMLDOM.GETNODEVALUE(text);
								DBMS_XMLDOM.FREENODE(text);
							
							ELSE IF DBMS_XMLDOM.GETNODENAME(field) = 'score' THEN
								text := DBMS_XMLDOM.GETFIRSTCHILD(field);
								score := DBMS_XMLDOM.GETNODEVALUE(text);
								DBMS_XMLDOM.FREENODE(text);
							END IF;
							END IF;
							DBMS_XMLDOM.FREENODE(attr);
						END LOOP;
                        
                        IF UPPER(translation) = 'TRUE' THEN
                                tranBool := TRUE;
                        ELSE
                                tranBool := FALSE;
                        END IF;
						
						IF useSnippet THEN
							BEGIN
								snippet :=  CTX_DOC.SNIPPET(index_Name, docName, query, start_Tag, end_Tag, tranBool, separator);
							EXCEPTION
								WHEN OTHERS THEN
								IF isDebug THEN
									DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(13));
									DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(10));
									DBMS_LOB.WRITEAPPEND(tmpLob,  36, 'Error while retrieving snippet for ''');
									DBMS_LOB.WRITEAPPEND(tmpLob,  length(docName),  docName);
									DBMS_LOB.WRITEAPPEND(tmpLob,  3,  ''': ');
									DBMS_LOB.WRITEAPPEND(tmpLob,  length(SQLERRM), SQLERRM);
								END IF;
								snippet := '';
							END;
						ELSE 
							snippet := '';
						END IF;

						tr := search_result_row(dID, docName, score, snippet);
						tt(j + 1) := tr;
						-- assemble query;
						IF (ns) THEN
							IF (isFirst = FALSE) THEN
								mq := mq || ')';
								mq := mq || ' UNION ';
							ELSE 
								isFirst := FALSE;
							END IF;
							mq := mq || ' SELECT ' || field_List || ' FROM ' || table_name || ' WHERE dID IN (' || to_clob(tr.dID);
							ns := FALSE;
						ELSE 
							mqlc := mqlc + 1;
							mq := mq || ',' || to_clob(tr.dID);
							IF (mqlc >= 1000) THEN
								mqlc := mqlc - 1000 + 1;
								ns := true;
							END IF;
						END IF;
					END IF;
					
					DBMS_XMLDOM.FREENODE(hit);
				END LOOP;

				IF mq IS NOT NULL THEN
					mq := mq || ')';
				END IF;
			END IF;
			END IF;
			
			DBMS_XMLDOM.FREENODE(node);
		END LOOP;
		IF isDebug THEN
			DBMS_LOB.WRITEAPPEND(tmpLob,  27, 'Result parsing   end time: ');               	
			DBMS_LOB.WRITEAPPEND(tmpLob,  11, to_char(systimestamp, 'HH24:MI:SSxFF'));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(13));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(10));
		END IF;
		
		IF (mq IS NULL) THEN
			mq := 'SELECT ' || field_List || ' FROM ' || table_name || ' WHERE 1 = 0';
		END IF;
		tmpLob := tmpLob ||  mq;
		IF isDebug THEN
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(13));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(10));
			DBMS_LOB.WRITEAPPEND(tmpLob,  23, 'Meta query start time: ');              	
			DBMS_LOB.WRITEAPPEND(tmpLob,  11, to_char(systimestamp, 'HH24:MI:SSxFF'));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(13));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(10));
		END IF;

		OPEN meta_result FOR mq;

		IF isDebug THEN
			DBMS_LOB.WRITEAPPEND(tmpLob,  20, 'Meta query end time: ');              	
			DBMS_LOB.WRITEAPPEND(tmpLob,  11, to_char(systimestamp, 'HH24:MI:SSxFF'));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(13));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(10));
			trace := tmpLob;
		END IF;    
        OPEN additional_result FOR SELECT * FROM TABLE(tt);
        
        DBMS_XMLDOM.FREEDOCUMENT(doc);
        DBMS_XMLPARSER.FREEPARSER(tp);
        DBMS_LOB.FREETEMPORARY(tmpLob);
        DBMS_LOB.FREETEMPORARY(mq);
	END;


PROCEDURE hays_search(index_name in VARCHAR2,
		table_name in VARCHAR2,
		query in VARCHAR2,
		field_list in VARCHAR2,
		result_set_descriptor IN CLOB,
		text_result OUT NOCOPY CLOB,
		meta_result OUT sys_refCURSOR,
		additional_result OUT sys_refcursor,
		rcount OUT INT,
		start_tag IN VARCHAR2,
		end_tag IN VARCHAR2,
		translation IN VARCHAR2,
		separator IN VARCHAR2,
		flags IN INT,
		trace OUT NOCOPY CLOB,
    ne_latitude NUMBER,
    ne_longitude NUMBER,
    sw_latitude NUMBER,
    sw_longitude NUMBER,
    radius NUMBER,
    exclude_equals_job_locations INTEGER DEFAULT 2) AS

	tp	DBMS_XMLPARSER.PARSER := dbms_xmlParser.newparser();
	doc	DBMS_XMLDOM.DOMDocument;
	node	DBMS_XMLDOM.DOMNODE;
	tmpNode DBMS_XMLDOM.DOMNODE;
	nl	DBMS_XMLDOM.DOMNODELIST;
	hit	DBMS_XMLDOM.DOMNODE;
	field	DBMS_XMLDOM.DOMNODE;
	hl	DBMS_XMLDOM.DOMNODELIST;
	fl	DBMS_XMLDOM.DOMNODELIST;
	flen	PLS_INTEGER;
	name	VARCHAR2 (100);
	len	INT;
	hlen	PLS_INTEGER;
	i	PLS_INTEGER;
	j	PLS_INTEGER;
	tr	search_result_row;
	tt	search_result_collection  := search_result_collection();
	nnm	DBMS_XMLDOM.DOMNAMEDNODEMAP;
	attr	DBMS_XMLDOM.DOMNODE;
	text	DBMS_XMLDOM.DOMNODE;

  cursor hays_locations(c_sw_longitude NUMBER, c_sw_latitude NUMBER, c_ne_longitude NUMBER, c_ne_latitude NUMBER) is 
  select l.location_id from rdfusr.hays_locations l
  where sdo_inside(l.geo_location, SDO_GEOMETRY(2003,  8307, NULL, SDO_ELEM_INFO_ARRAY(1,1003,3), SDO_ORDINATE_ARRAY(c_sw_longitude, c_sw_latitude, c_ne_longitude, c_ne_latitude))) =  'TRUE'
  --and rownum < 11
  ;
  
  cursor hays_locations_radius(c_ne_longitude NUMBER, c_ne_latitude NUMBER, c_radius NUMBER) is 
  select l.location_id from rdfusr.hays_locations l
  where sdo_within_distance (l.geo_location, SDO_GEOMETRY(2001, 8307, SDO_POINT_TYPE (c_ne_longitude, c_ne_latitude,NULL), NULL, NULL), 'distance = '||c_radius|| ' unit=MILE') = 'TRUE'
  --and rownum < 51
  ;
  
  cursor hays_locations_exclude(c_ne_longitude NUMBER, c_ne_latitude NUMBER, c_radius NUMBER) is 
  select l.location_id from rdfusr.hays_locations l             
  where sdo_within_distance (l.geo_location, SDO_GEOMETRY(2001, 8307, SDO_POINT_TYPE (c_ne_longitude,c_ne_latitude ,NULL), NULL, NULL), 'distance = '||c_radius+300||' unit=MILE') = 'TRUE'
  and l.location_id not in
  (select l.location_id from rdfusr.hays_locations l
  where sdo_within_distance (l.geo_location, SDO_GEOMETRY(2001, 8307, SDO_POINT_TYPE (c_ne_longitude, c_ne_latitude,NULL), NULL, NULL), 'distance = '||c_radius|| ' unit=MILE') = 'TRUE')
  ;
  
  hays_var_location varchar2(4000); 

	mq	CLOB; 
	tmpLob CLOB;
	isFirst	BOOLEAN := TRUE;
	ns	BOOLEAN := TRUE;
	mqlc	PLS_INTEGER := 1;
        
        
	tranBool BOOLEAN := FALSE;

	dID	INT;
	docName	VARCHAR2 (100);
	score	INT;
	snippet	VARCHAR2 (4000);

	isDebug	BOOLEAN := FALSE;
	useSnippet	BOOLEAN := FALSE;
	dcount	VARCHAR(100);
	dl	DBMS_XMLDOM.DOMNODELIST;
	dValue	VARCHAR2 (1000);
  --hays_var_all_locations varchar2(32767);
  hays_var_all_locations2 clob;
  hays_query clob;
	BEGIN
		isDebug := true;--check_flag(flags, 1);
		useSnippet := check_flag(flags, 2);

		dbms_lob.createtemporary(tmpLob, true, DBMS_LOB.CALL);
		dbms_lob.createtemporary(text_result, true, DBMS_LOB.CALL);
		IF useSnippet THEN
			DBMS_LOB.WRITEAPPEND(tmpLob,  21, 'HAYS Snippet enabled.');
		END IF;
		IF isDebug THEN
			DBMS_LOB.WRITEAPPEND(tmpLob,  1, CHR(13));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1, CHR(10));
			DBMS_LOB.WRITEAPPEND(tmpLob,  38, 'HAYS ctx_query.result_set start time: ');
			DBMS_LOB.WRITEAPPEND(tmpLob,  11, to_char(systimestamp, 'HH24:MI:SSxFF'));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1, CHR(13));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1, CHR(10));
		END IF;
    
    dbms_lob.createtemporary(hays_var_all_locations2, TRUE, DBMS_LOB.CALL);
    --hays_var_all_locations2 := empty_clob();
    hays_query:=query; --initialize
    IF isDebug THEN
      DBMS_LOB.WRITEAPPEND(tmpLob,  37, 'HAYS exclude_equals_job_locations:: '||exclude_equals_job_locations);--||hays_locations_exclude%rowcount
      DBMS_LOB.WRITEAPPEND(tmpLob,  1, CHR(13));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1, CHR(10));
    end if;
    if not (ne_latitude=0  AND ne_longitude=0  AND sw_latitude=0  AND sw_longitude=0 ) then --if none of them is provided dont spatial
      
        if (sw_latitude=0  AND sw_longitude=0) then  --circle search
          if (exclude_equals_job_locations=1) then  --do exclude search
              open hays_locations_exclude(ne_longitude, ne_latitude, radius);
                IF isDebug THEN
                    DBMS_LOB.WRITEAPPEND(tmpLob,  21, 'HAYS locations found:');--||hays_locations_exclude%rowcount
                    DBMS_LOB.WRITEAPPEND(tmpLob,  1, CHR(13));
                    DBMS_LOB.WRITEAPPEND(tmpLob,  1, CHR(10));
                end if;
                Loop
                   FETCH hays_locations_exclude INTO hays_var_location;
                    Exit when hays_locations_exclude%notfound;
                    --hays_var_all_locations := hays_var_all_locations || ' or ' || hays_var_location;
                    hays_var_location := ','|| hays_var_location;
                    dbms_lob.writeappend(hays_var_all_locations2, length(hays_var_location), hays_var_location);
                end loop;   
                close hays_locations_exclude;
          else  --search within circle
                --hays_var_all_locations2 := '';
                IF isDebug THEN
                    DBMS_LOB.WRITEAPPEND(tmpLob,  29, 'HAYS locations circle search:');--||hays_locations_exclude%rowcount
                    DBMS_LOB.WRITEAPPEND(tmpLob,  1, CHR(13));
                    DBMS_LOB.WRITEAPPEND(tmpLob,  1, CHR(10));
                end if;
                open hays_locations_radius(ne_longitude, ne_latitude, radius);
                Loop
                   FETCH hays_locations_radius INTO hays_var_location;
                    Exit when hays_locations_radius%notfound;
                    --hays_var_all_locations := hays_var_all_locations || ' or ' || hays_var_location;
                    hays_var_location := ','|| hays_var_location;
                    dbms_lob.writeappend(hays_var_all_locations2, length(hays_var_location), hays_var_location);
                end loop;   
                close hays_locations_radius;
           end if;     
      else                                                  --search within rectangle
          --hays_var_all_locations2 := '';
          open hays_locations(sw_longitude, sw_latitude, ne_longitude, ne_latitude);
          Loop
             FETCH hays_locations INTO hays_var_location;
              Exit when hays_locations%notfound;
              hays_var_location := ','|| hays_var_location;
              dbms_lob.writeappend(hays_var_all_locations2, length(hays_var_location),hays_var_location);
          end loop;   
          close hays_locations;
      end if;
       -- hays_var_all_locations2 := substr(hays_var_all_locations2,2);
        IF isDebug THEN
          DBMS_LOB.WRITEAPPEND(tmpLob,  23, 'HAYS spatial end time: ');
          DBMS_LOB.WRITEAPPEND(tmpLob,  11, to_char(systimestamp, 'HH24:MI:SSxFF'));
        --  DBMS_LOB.WRITEAPPEND(tmpLob,  29, 'HAYS all locations variable: ');
          --DBMS_LOB.WRITEAPPEND(tmpLob,  length(hays_var_all_locations), hays_var_all_locations);  
        --DBMS_LOB.WRITEAPPEND(tmpLob,  29, 'HAYS all locations variable2: ');
        --  DBMS_LOB.WRITEAPPEND(tmpLob,  length(hays_var_all_locations2), hays_var_all_locations2);  
        end if;
        
        if length(hays_var_all_locations2) = 0 then
          dbms_lob.writeappend(hays_var_all_locations2, length(',NO_MATCHING_LOC'),',NO_MATCHING_LOC');
        end if;          
        
        if length(query) = 0 then
         --hays_query:=query;
          hays_query := '((' || substr(hays_var_all_locations2, 2) || ') WITHIN xLocation)';
        else
         hays_query := substr( query, 0, length(query)-1)|| ' and ((' || substr(hays_var_all_locations2, 2) || ') WITHIN xLocation))';
        end if;
        
        /*IF isDebug THEN
          DBMS_LOB.WRITEAPPEND(tmpLob,  17, 'HAYS full query: ');
          DBMS_LOB.WRITEAPPEND(tmpLob,  length(hays_query), hays_query);  
        end if;*/
      
    end if;
    
		ctx_query.result_set_clob_query(index_name, hays_query, result_set_descriptor, text_result);

		IF isDebug THEN
			DBMS_LOB.WRITEAPPEND(tmpLob,  32, 'HAYS ctx_query.result_set   end time: ');  
			DBMS_LOB.WRITEAPPEND(tmpLob,  11, to_char(systimestamp, 'HH24:MI:SSxFF'));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(13));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(10));
		END IF;		
		DBMS_XMLPARSER.PARSECLOB(tp, text_result);
		doc := DBMS_XMLPARSER.GETDOCUMENT(tp);
		tmpNode := DBMS_XMLDOM.MAKENODE(doc);

--		 This is count;
		node := DBMS_XMLDOM.GETFIRSTCHILD(tmpNode);
		DBMS_XMLDOM.FREENODE(tmpNode);
		
		nl := DBMS_XMLDOM.GETCHILDNODES(node);
		len := DBMS_XMLDOM.GETLENGTH(nl);	
		DBMS_XMLDOM.FREENODE(node);

		IF isDebug THEN
            DBMS_LOB.WRITEAPPEND(tmpLob,  27, 'Result parsing start time: ');                 	
			DBMS_LOB.WRITEAPPEND(tmpLob,  11, to_char(systimestamp, 'HH24:MI:SSxFF'));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(13));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(10));
		END IF;

		FOR i IN 0..len - 1 LOOP
			node := DBMS_XMLDOM.ITEM(nl, i);
			name := DBMS_XMLDOM.GETNODENAME(node);	

			IF name = 'count' THEN
--				dbms_output.put_line('found count node');
				text := DBMS_XMLDOM.GETFIRSTCHILD(node);
				rcount := DBMS_XMLDOM.GETNODEVALUE(text);	
				DBMS_XMLDOM.FREENODE(text);
			ELSE IF name = 'hitlist' THEN

				hl := DBMS_XMLDOM.GETCHILDNODES(node);
				hlen := DBMS_XMLDOM.GETLENGTH(hl);
				tt.extend(hlen);

				FOR j IN 0..hlen - 1 LOOP
					hit := DBMS_XMLDOM.ITEM(hl, j);
					name := DBMS_XMLDOM.GETNODENAME(hit);
					IF name = 'hit' THEN
						fl := DBMS_XMLDOM.GETCHILDNODES(hit);
						flen := DBMS_XMLDOM.GETLENGTH(fl);
						dID := -1;
						score := -1;
						docName := '';
						snippet := '';
						FOR k IN 0..flen - 1 LOOP
						
							field := DBMS_XMLDOM.ITEM(fl, k);							
							
							nnm := DBMS_XMLDOM.GETATTRIBUTES(field);
							attr := DBMS_XMLDOM.GETNAMEDitem(nnm, 'name');
							name := DBMS_XMLDOM.GETNODEVALUE(attr);
							
							IF name = 'DID' THEN
								text := DBMS_XMLDOM.GETFIRSTCHILD(field);
								dID := DBMS_XMLDOM.GETNODEVALUE(text);
								DBMS_XMLDOM.FREENODE(text);
							END IF;
							IF name = 'SDDDOCNAME' THEN
								text := DBMS_XMLDOM.GETFIRSTCHILD(field);
								docName := DBMS_XMLDOM.GETNODEVALUE(text);
								DBMS_XMLDOM.FREENODE(text);
							
							ELSE IF DBMS_XMLDOM.GETNODENAME(field) = 'score' THEN
								text := DBMS_XMLDOM.GETFIRSTCHILD(field);
								score := DBMS_XMLDOM.GETNODEVALUE(text);
								DBMS_XMLDOM.FREENODE(text);
							END IF;
							END IF;
							DBMS_XMLDOM.FREENODE(attr);
						END LOOP;
                        
                        IF UPPER(translation) = 'TRUE' THEN
                                tranBool := TRUE;
                        ELSE
                                tranBool := FALSE;
                        END IF;
						
						IF useSnippet THEN
							BEGIN
								snippet :=  CTX_DOC.SNIPPET(index_Name, docName, query, start_Tag, end_Tag, tranBool, separator);
							EXCEPTION
								WHEN OTHERS THEN
								IF isDebug THEN
									DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(13));
									DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(10));
									DBMS_LOB.WRITEAPPEND(tmpLob,  36, 'Error while retrieving snippet for ''');
									DBMS_LOB.WRITEAPPEND(tmpLob,  length(docName),  docName);
									DBMS_LOB.WRITEAPPEND(tmpLob,  3,  ''': ');
									DBMS_LOB.WRITEAPPEND(tmpLob,  length(SQLERRM), SQLERRM);
								END IF;
								snippet := '';
							END;
						ELSE 
							snippet := '';
						END IF;

						tr := search_result_row(dID, docName, score, snippet);
						tt(j + 1) := tr;
						-- assemble query;
						IF (ns) THEN
							IF (isFirst = FALSE) THEN
								mq := mq || ')';
								mq := mq || ' UNION ';
							ELSE 
								isFirst := FALSE;
							END IF;
							mq := mq || ' SELECT ' || field_List || ' FROM ' || table_name || ' WHERE dID IN (' || to_clob(tr.dID);
							ns := FALSE;
						ELSE 
							mqlc := mqlc + 1;
							mq := mq || ',' || to_clob(tr.dID);
							IF (mqlc >= 1000) THEN
								mqlc := mqlc - 1000 + 1;
								ns := true;
							END IF;
						END IF;
					END IF;
					
					DBMS_XMLDOM.FREENODE(hit);
				END LOOP;

				IF mq IS NOT NULL THEN
					mq := mq || ')';
				END IF;
			END IF;
			END IF;
			
			DBMS_XMLDOM.FREENODE(node);
		END LOOP;
		IF isDebug THEN
			DBMS_LOB.WRITEAPPEND(tmpLob,  27, 'Result parsing   end time: ');               	
			DBMS_LOB.WRITEAPPEND(tmpLob,  11, to_char(systimestamp, 'HH24:MI:SSxFF'));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(13));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(10));
		END IF;
		
		IF (mq IS NULL) THEN
			mq := 'SELECT ' || field_List || ' FROM ' || table_name || ' WHERE 1 = 0';
		END IF;
		tmpLob := tmpLob ||  mq;
		IF isDebug THEN
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(13));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(10));
			DBMS_LOB.WRITEAPPEND(tmpLob,  23, 'Meta query start time: ');              	
			DBMS_LOB.WRITEAPPEND(tmpLob,  11, to_char(systimestamp, 'HH24:MI:SSxFF'));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(13));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(10));
		END IF;

		OPEN meta_result FOR mq;

		IF isDebug THEN
			DBMS_LOB.WRITEAPPEND(tmpLob,  20, 'Meta query end time: ');              	
			DBMS_LOB.WRITEAPPEND(tmpLob,  11, to_char(systimestamp, 'HH24:MI:SSxFF'));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(13));
			DBMS_LOB.WRITEAPPEND(tmpLob,  1,  CHR(10));
			trace := tmpLob;
		END IF;    
        OPEN additional_result FOR SELECT * FROM TABLE(tt);
        
        DBMS_XMLDOM.FREEDOCUMENT(doc);
        DBMS_XMLPARSER.FREEPARSER(tp);
        DBMS_LOB.FREETEMPORARY(tmpLob);
        DBMS_LOB.FREETEMPORARY(mq);
	END;
$END		
END;

/

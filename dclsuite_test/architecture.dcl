% -----------------------------------
% Definição dos módulos:
% -----------------------------------

%module A001: com.example.a.A001
%module A002: com.example.a.A002
%module A003: com.example.a.A003
%module A004: com.example.a.A004
%module A009: com.example.a.A009
%module A010: com.example.a.A010
%module A011: com.example.a.A011
%module A012: com.example.a.A012
%module A013: com.example.a.A013
%module A014: com.example.a.A014
%module A015: com.example.a.A015
%module A016: com.example.a.A016

%module B001: com.example.b.B001
%module B002: com.example.b.B002
%module B003: com.example.b.B003
%module B004: com.example.b.B004
%module B005: com.example.b.B005
%module B006: com.example.b.B006
%module B007: com.example.b.B007
%module B008: com.example.b.B008
%module B009: com.example.b.B009
%module B010: com.example.b.B010
%module B011: com.example.b.B011
%module B012: com.example.b.B012
%module B013: com.example.b.B013
%module B014: com.example.b.B014
%module B015: com.example.b.B015
%module B016: com.example.b.B016

%module C005: com.example.c.C005
%module C006: com.example.c.C006
%module C007: com.example.c.C007
%module C008: com.example.c.C008

% -----------------------------------
% Access:
% -----------------------------------

%A001 can-access-only B001, $java
%A002 can-access-only B002, $java
%A003 can-access-only B003
%A004 can-access-only B004
 
%only C005 can-access B005
%only C006 can-access B006
%only C007 can-access B007
%only C008 can-access B008
  

%A009 cannot-access B009
%A010 cannot-access B010
%A011 cannot-access B011
%A012 cannot-access B012

%A013 must-access B013
%A014 must-access B014
%A015 must-access B015
%A016 must-access B016
# Proyecto Laboratorio — Gestión de equipos (JavaFX + Hibernate/JPA + PostgreSQL)

Aplicación de escritorio para registrar, actualizar, eliminar y consultar los equipos de un laboratorio, clasificados por tipo de equipo. La interfaz está hecha con **JavaFX (FXML)** y la persistencia con **Hibernate ORM (JPA)** sobre **PostgreSQL**.

**Repositorio:** https://github.com/Andresmejia62/ProyectoLaboratorio

---

## 1. Requisitos de ejecución

| Herramienta | Versión usada / mínima |
|---|---|
| JDK | 17 o superior (probado con OpenJDK 21) |
| Maven | 3.9+ (o el wrapper incluido `./mvnw`) |
| PostgreSQL | 13+ (probado con PostgreSQL 15) |
| IDE (opcional) | IntelliJ IDEA / NetBeans / VS Code con soporte Maven |

Dependencias principales (descargadas por Maven, ver `pom.xml`):

- `org.openjfx:javafx-controls` y `javafx-fxml` 21.0.6
- `org.hibernate.orm:hibernate-core` 7.4.12.Final
- `org.postgresql:postgresql` 42.7.14

---

## 2. Configuración local de PostgreSQL

### 2.1 Crear la base de datos

```bash
createdb bd_laboratorio_orm
```

(o en `psql`: `CREATE DATABASE bd_laboratorio_orm;`)

### 2.2 Crear el esquema, las tablas y los datos de ejemplo

El script está en `src/main/java/com/appregistros/proyectolaboratorio/database/Transact`. Crea:

- el esquema `inventario`,
- la tabla `inventario.tipo_equipo`,
- la tabla `inventario.equipo` con sus restricciones (`UNIQUE` en código, `CHECK` de cantidad y estado, llave foránea a `tipo_equipo`),
- 4 tipos de equipo y 10 equipos de ejemplo.

```bash
psql -d bd_laboratorio_orm -f src/main/java/com/appregistros/proyectolaboratorio/database/Transact
```

> Nota: las dos primeras sentencias del script (`CREATE SCHEMA` y `CREATE TABLE inventario.tipo_equipo`) no terminan en `;`. Si `psql` muestra un error de sintaxis, agrega el punto y coma al final de cada una o ejecútalas una por una.

### 2.3 Credenciales de conexión

La conexión se resuelve en `database/JPAUtil.java`. Primero lee estas variables de entorno y, si no existen, usa los valores por defecto del código:

| Variable | Valor por defecto |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/bd_laboratorio_orm` |
| `DB_USER` | usuario local del autor |
| `DB_PASSWORD` | contraseña local del autor |

Para usar tus propias credenciales sin modificar el código:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/bd_laboratorio_orm
export DB_USER=postgres
export DB_PASSWORD=tu_contraseña
```

En IntelliJ también puedes definirlas en *Run → Edit Configurations → Environment variables*.

### 2.4 Verificar la conexión

La clase `database/DBtester.java` abre una conexión JDBC con esos mismos datos y muestra `Conexion correcta: <url>` si todo está bien. Ejecútala desde el IDE (botón ▶ sobre su método `main`).

---

## 3. Pasos para iniciar la aplicación

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/Andresmejia62/ProyectoLaboratorio.git
   ```
   ```bash
   cd ProyectoLaboratorio
   ```
2. Preparar la base de datos (sección 2).
3. Compilar y descargar dependencias:
   ```bash
   ./mvnw clean compile
   ```
4. Ejecutar la clase **`com.appregistros.proyectolaboratorio.Launcher`** desde el IDE (clic derecho → *Run 'Launcher.main()'*). `Launcher` arranca `EquipoApplication`, que carga la vista `equipo-view.fxml`.

> Sobre `mvn javafx:run`: en el `pom.xml` el `mainClass` del plugin `javafx-maven-plugin` todavía apunta a la plantilla original (`com.appregistros.proyectohibernate/...HelloApplication`). Para usar ese comando hay que cambiarlo a `com.appregistros.proyectolaboratorio/com.appregistros.proyectolaboratorio.Launcher`.

### Uso de la ventana

- **Guardar**: registra un equipo nuevo con los datos del formulario.
- **Actualizar**: seleccionar una fila de la tabla, modificar los campos y pulsar *Actualizar*.
- **Eliminar**: seleccionar una fila, pulsar *Eliminar* y confirmar el diálogo.
- **Limpiar**: vacía el formulario.
- **Buscar / Aplicar filtros**: filtra la tabla por nombre (búsqueda parcial, sin distinguir mayúsculas) y/o por tipo de equipo (`Todos` = sin filtro).

---

## 4. Arquitectura

```
src/main/java/com/appregistros/proyectolaboratorio/
├── Launcher.java / EquipoApplication.java   → arranque JavaFX
├── controller/EquipoController.java          → lógica de la vista y validaciones
├── Dao/EquipoDao.java, TipoEquipoDao.java     → acceso a datos (JPA / JPQL)
├── model/Equipo.java, TipoEquipo.java         → entidades JPA
└── database/JPAUtil.java, DBtester.java, Transact (script SQL)
src/main/resources/
├── META-INF/persistence.xml                   → unidad de persistencia "laboratorio_orm"
└── com/appregistros/proyectolaboratorio/equipo-view.fxml
```

Flujo: **Vista (FXML) → Controlador → DAO → EntityManager (Hibernate) → PostgreSQL**. Cada operación del DAO abre su propio `EntityManager`, y las escrituras van dentro de una transacción con `rollback` si hay error.

---

## 5. Entidades

### `TipoEquipo` → tabla `inventario.tipo_equipo`

| Atributo | Columna | Restricción |
|---|---|---|
| `idTipo` (Long) | `id_tipo` | PK, identidad autogenerada |
| `nombre` (String) | `nombre` | `NOT NULL`, `UNIQUE`, máx. 100 |
| `descripcion` (String) | `descripcion` | máx. 150 |
| `equipos` (List&lt;Equipo&gt;) | — | lado inverso de la relación (`mappedBy = "tipoEquipo"`) |

### `Equipo` → tabla `inventario.equipo`

| Atributo | Columna | Restricción |
|---|---|---|
| `idEquipo` (Long) | `id_equipo` | PK, identidad autogenerada |
| `codigo` (String) | `codigo` | `NOT NULL`, `UNIQUE`, máx. 10 |
| `nombre` (String) | `nombre` | `NOT NULL`, máx. 100 |
| `marca` (String) | `marca` | opcional, máx. 100 |
| `estado` (String) | `estado` | `NOT NULL`, `CHECK` (Disponible / En mantenimiento / Fuera de servicio) |
| `cantidad` (Integer) | `cantidad` | `NOT NULL`, `CHECK (cantidad >= 0)` |
| `tipoEquipo` (TipoEquipo) | `id_tipo` | FK `fk_equipo_tipo_equipo` → `tipo_equipo.id_tipo`, `NOT NULL` |

---

## 6. Asociación

La relación entre las entidades es **bidireccional de muchos a uno / uno a muchos**:

- **`Equipo` → `TipoEquipo`**: `@ManyToOne(fetch = FetchType.EAGER)` con `@JoinColumn(name = "id_tipo", nullable = false)`. Es el **lado propietario**: la llave foránea `id_tipo` vive en la tabla `equipo`. Un equipo siempre pertenece a un tipo.
- **`TipoEquipo` → `Equipo`**: `@OneToMany(mappedBy = "tipoEquipo")`. Es el **lado inverso**: un tipo agrupa varios equipos y no genera columnas adicionales.
- Los métodos auxiliares `addEquipo()` / `removeEquipo()` de `TipoEquipo` mantienen sincronizados ambos lados en memoria.

```
tipo_equipo (1) ──────< (N) equipo
   id_tipo  ◄──────────── id_tipo (FK)
```

---

## 7. Consultas (JPQL)

| Método | Consulta | Uso |
|---|---|---|
| `EquipoDao.listarTodos()` | `SELECT e FROM Equipo e JOIN FETCH e.tipoEquipo t ORDER BY e.idEquipo` | Cargar la tabla con su tipo en una sola consulta (evita el problema N+1). |
| `EquipoDao.buscarPorCodigo(codigo)` | `SELECT e FROM Equipo e WHERE e.codigo = :codigo` | Validar código duplicado antes de guardar o actualizar. |
| `EquipoDao.buscarPorTipo(idTipo)` | `... JOIN FETCH e.tipoEquipo t WHERE t.idTipo = :idTipo ORDER BY e.idEquipo` | Equipos de un tipo específico. |
| `EquipoDao.buscarPorFiltros(nombre, idTipo)` | JPQL dinámico: `... WHERE 1 = 1` + `AND LOWER(e.nombre) LIKE LOWER(:nombre)` + `AND t.idTipo = :idTipo` | Búsqueda parcial por nombre combinable con filtro por tipo. |
| `EquipoDao.buscarPorId(id)` | `em.find(Equipo.class, id)` | Recuperar un equipo para actualizar o eliminar. |
| `TipoEquipoDao.listarTodos()` | `SELECT t FROM TipoEquipo t ORDER BY t.idTipo` | Llenar los `ComboBox` de tipo y filtro. |
| `TipoEquipoDao.buscarPorNombre(nombre)` | `SELECT t FROM TipoEquipo t WHERE t.nombre = :nombre` | Buscar un tipo por su nombre. |

Todas las consultas usan **parámetros con nombre** (`:param`), lo que evita inyección SQL.

---

## 8. Matriz de pruebas P1–P8

Las pruebas se ejecutaron el **13/11/2026** sobre la **interfaz real** (`equipo-view.fxml` + `EquipoController`), conectada a la base local `bd_laboratorio_orm` (PostgreSQL 15.19), que al empezar tenía 10 equipos y 4 tipos de ejemplo. El script `evidencias/script/PruebasUI.java` llena el formulario, pulsa los botones de la ventana y guarda una captura en cada paso. Los resultados en la base se comprobaron con `psql`.

**Evidencias:**

- Capturas de pantalla `P1_…png` a `P8_…png` en [`src/main/java/com/appregistros/proyectolaboratorio/Evidencias/`](src/main/java/com/appregistros/proyectolaboratorio/Evidencias/).
- [`evidencias/consultas_postgresql.txt`](evidencias/consultas_postgresql.txt): consultas SQL que confirman cada resultado directamente en PostgreSQL.

| ID | Prueba mínima | Datos usados | Resultado esperado | Resultado obtenido | Estado | Evidencia |
|---|---|---|---|---|---|---|
| P1 | Registrar un equipo válido | `EQ-011`, Monitor 24 pulgadas, LG, Computo, Disponible, 4 | Registro guardado y visible. | Mensaje "El equipo se guardó correctamente"; EQ-011 aparece en la tabla. | ✅ Aprobada | [P1_1](src/main/java/com/appregistros/proyectolaboratorio/Evidencias/P1_1_formulario.png), [P1_2](src/main/java/com/appregistros/proyectolaboratorio/Evidencias/P1_2_guardado.png), [P1_3](src/main/java/com/appregistros/proyectolaboratorio/Evidencias/P1_3_visible_en_tabla.png) |
| P2 | Intentar código duplicado | Código `EQ-001` (ya existe) | Operación rechazada; sin duplicados. | Alerta "Código duplicado: Ya existe un equipo con ese código". En PostgreSQL, `EQ-001` aparece 1 sola vez. | ✅ Aprobada | [P2](src/main/java/com/appregistros/proyectolaboratorio/Evidencias/P2_codigo_duplicado.png), consultas_postgresql.txt |
| P3 | Editar nombre, estado o tipo | EQ-011 → nombre "Monitor 27 pulgadas", estado Fuera de servicio, tipo Audiovisual | Cambios persistidos. | Mensaje "El equipo se actualizó correctamente"; tabla y PostgreSQL muestran los 3 cambios. | ✅ Aprobada | [P3_1](src/main/java/com/appregistros/proyectolaboratorio/Evidencias/P3_1_antes.png), [P3_2](src/main/java/com/appregistros/proyectolaboratorio/Evidencias/P3_2_actualizado.png), [P3_3](src/main/java/com/appregistros/proyectolaboratorio/Evidencias/P3_3_despues.png), consultas_postgresql.txt |
| P4 | Eliminar un equipo con confirmación | `EQ-012` Teclado USB (creado para esta prueba) | El registro desaparece del listado y de PostgreSQL. | Diálogo "¿Desea eliminar Teclado USB?" → OK → "Equipo eliminado". EQ-012 ya no está en la tabla y `SELECT … WHERE codigo='EQ-012'` devuelve 0 filas. | ✅ Aprobada | [P4_1](src/main/java/com/appregistros/proyectolaboratorio/Evidencias/P4_1_seleccionado.png), [P4_2](src/main/java/com/appregistros/proyectolaboratorio/Evidencias/P4_2_confirmacion.png), [P4_3](src/main/java/com/appregistros/proyectolaboratorio/Evidencias/P4_3_eliminado.png), [P4_4](src/main/java/com/appregistros/proyectolaboratorio/Evidencias/P4_4_tabla_sin_registro.png), consultas_postgresql.txt |
| P5 | Buscar por nombre parcial | Texto `laptop`, tipo Todos | Solo se muestran coincidencias. | Solo EQ-001 "Laptop Latitude 5420" y EQ-003 "Laptop ThinkPad E14" (sin distinguir mayúsculas). | ✅ Aprobada | [P5](src/main/java/com/appregistros/proyectolaboratorio/Evidencias/P5_busqueda_parcial.png) |
| P6 | Filtrar por tipo y combinar con búsqueda | Texto `pr`; luego `pr` + tipo Impresion | Resultados cumplen ambas condiciones. | Solo texto: EQ-007 Impresora y EQ-009 Proyector. Texto + Impresion: solo EQ-007. | ✅ Aprobada | [P6_1](src/main/java/com/appregistros/proyectolaboratorio/Evidencias/P6_1_solo_texto.png), [P6_2](src/main/java/com/appregistros/proyectolaboratorio/Evidencias/P6_2_texto_y_tipo.png) |
| P7 | Ingresar cantidad negativa | Cantidad `-5` | Validación rechaza el dato. | Alerta "Cantidad inválida: La cantidad no puede ser negativa"; no se guarda nada (además la BD tiene `CHECK (cantidad >= 0)`). | ✅ Aprobada | [P7](src/main/java/com/appregistros/proyectolaboratorio/Evidencias/P7_cantidad_negativa.png), consultas_postgresql.txt |
| P8 | Reiniciar aplicación | Se cierra la app y se abre en un proceso nuevo | Persisten los datos previamente confirmados. | Al volver a abrir, EQ-011 aparece con los cambios de P3 (Monitor 27 pulgadas, Fuera de servicio, Audiovisual). | ✅ Aprobada | [P8](src/main/java/com/appregistros/proyectolaboratorio/Evidencias/P8_reinicio.png), consultas_postgresql.txt |

### Cómo repetir las pruebas

Con la base preparada (sección 2), desde la raíz del proyecto:

```bash
bash evidencias/script/ejecutar.sh
```

El script compila el proyecto, ejecuta P1–P7 en la ventana, la cierra, la vuelve a abrir para P8 y regenera las capturas y `consultas_postgresql.txt`. Antes de volver a ejecutarlo hay que borrar `EQ-011`, porque P1 lo registra:

```bash
psql -d bd_laboratorio_orm -c "DELETE FROM inventario.equipo WHERE codigo = 'EQ-011';"
```

### Corrección aplicada a partir de las pruebas

Al probar se encontró que el `ComboBox` de estado ofrecía `Disponible, Prestado, En mantenimiento, Dañado, Baja`, pero la restricción `chk_estado` de PostgreSQL solo acepta `Disponible, En mantenimiento, Fuera de servicio`. Por eso, guardar con Prestado, Dañado o Baja fallaba en la base, y no se podía elegir "Fuera de servicio".

Se corrigió en `EquipoController`: la lista `ESTADOS_PERMITIDOS` ahora contiene exactamente los tres estados de `chk_estado`, y el `ComboBox` se llena con esa misma lista. La prueba P3 cambia el estado a **Fuera de servicio** y confirma que se guarda en la base.

---

## 9. Autor

Andrés Mejía — Proyecto de laboratorio (JavaFX + Hibernate + PostgreSQL).

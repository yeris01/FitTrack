# FitTrack - Tu Compañero de Entrenamiento Inteligente

FitTrack es una aplicación Android moderna diseñada para ayudarte a alcanzar tus objetivos de fitness. Con una interfaz elegante de alta fidelidad y un sistema de seguimiento robusto, FitTrack te permite gestionar tus rutinas, visualizar tu progreso y mantener la disciplina día a día.

## 🚀 Características Principales

- **Autenticación Local Segura:** Sistema de login y registro 100% local utilizando Room y SharedPreferences para proteger tu privacidad.
- **Aislamiento de Datos:** Cada usuario tiene su propio espacio de datos, asegurando que tus rutinas y progresos sean solo tuyos.
- **Gestión de Rutinas:**
    - **Sugerencias Inteligentes:** Comienza rápido con rutinas predefinidas (Full Body, Tren Superior, Tren Inferior).
    - **Rutinas Personalizadas:** Crea tus propias rutinas seleccionando entre una amplia lista de ejercicios por grupo muscular.
- **Entrenamiento Activo:**
    - Cronómetro en tiempo real.
    - Registro de series (peso y repeticiones) sobre la marcha.
    - **Temporizador de Descanso:** Barra visual de descanso para optimizar tus pausas entre series.
- **Visualización de Progreso:**
    - **Gráficos Dinámicos:** Observa tu evolución en cargas máximas por ejercicio.
    - **Calendario de Actividad:** Historial visual tipo "contribuciones" para ver qué días has entrenado.
    - **Historial Detallado:** Revisa cada sesión pasada, duración y volumen total levantado.
- **Objetivos Semanales:** Define cuántos días quieres entrenar y haz un seguimiento de tu constancia.
- **FitTrack Premium (IA):**
    - Generación de rutinas automáticas mediante Inteligencia Artificial adaptadas a tu nivel y equipo.
    - Estadísticas avanzadas y experiencia sin anuncios.

## 🎨 Diseño Visual

La aplicación cuenta con un diseño de **Alta Fidelidad** basado en una paleta de colores **Violeta/Púrpura**, con contrastes optimizados para facilitar la lectura durante el entrenamiento. Incluye fondos dinámicos y tarjetas interactivas con sutiles degradados.

## 🛠️ Stack Tecnológico

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose (Moderno, declarativo y fluido)
- **Base de Datos:** Room (Persistencia local eficiente)
- **Arquitectura:** MVVM (Model-View-ViewModel) para un código limpio y mantenible
- **Navegación:** Jetpack Navigation Component
- **Inyección de Dependencias:** ViewModel Factory manual (simplificado para portabilidad)
- **Monetización:** Google Play Billing Library & AdMob Integration

## 📥 Instalación y Configuración

1. Clona este repositorio o descarga el código fuente.
2. Abre el proyecto en **Android Studio (Koala o superior)**.
3. Asegúrate de tener configurado el SDK de Android (API 26 mínimo).
4. Sincroniza el proyecto con Gradle.
5. Haz clic en **Run** para desplegar en tu dispositivo físico o emulador.

---
Desarrollado con ❤️ para amantes del fitness. ¡A entrenar! 🏋️‍♂️🔥

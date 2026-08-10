# Frontend de Adulto Funcional

Este directorio reúne los clientes que consumen la API REST de Adulto
Funcional. Los dos clientes comparten el mismo dominio de negocio, pero se
mantienen como proyectos independientes para que cada plataforma pueda usar
las capacidades y el ciclo de entrega que le corresponden.

## Proyectos

| Proyecto | Tecnología | Estado |
|---|---|---|
| [`movil`](./movil) | Expo SDK 54, React Native y JavaScript | Desarrollo activo |
| [`web`](./web) | React, Vite y TypeScript | Scaffold; desarrollo pospuesto |

El desarrollo comienza por el cliente móvil. La interfaz web permanecerá en
su scaffold hasta que se defina su experiencia visual.

## Flujo de ramas

Los dos proyectos pertenecen al repositorio raíz, por lo que comparten una
misma rama Git. El trabajo nuevo nace de `develop` en ramas `feature/*`, se
integra nuevamente en `develop` y solo llega a `main` mediante una release.

La rama inicial del frontend es `feature/frontend-foundation`. Ningún flujo
automatizado de este directorio realiza `git push`.

## Backend

Los clientes se conectan al servidor Spring Boot disponible en `../server`.
La URL cambia según el dispositivo:

- Emulador Android: normalmente `http://10.0.2.2:8080`.
- Simulador iOS: normalmente `http://localhost:8080`.
- Expo Go en un teléfono: `http://<IP-LAN-DEL-EQUIPO>:8080`.

El teléfono y el equipo que ejecuta el backend deben estar en la misma red y
el puerto del backend debe ser accesible desde esa red.

